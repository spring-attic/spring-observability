/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.observability.tracing.reporter.wavefront;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import brave.handler.MutableSpan;
import brave.propagation.TraceContext;
import com.wavefront.sdk.common.Pair;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.entities.histograms.HistogramGranularity;
import com.wavefront.sdk.entities.tracing.SpanLog;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.observability.tracing.brave.bridge.BraveFinishedSpan;
import org.springframework.observability.tracing.brave.bridge.BraveTraceContext;

import static org.assertj.core.api.BDDAssertions.then;

class WavefrontSleuthSpanHandlerTests {

	@Test
	void should_delegate_to_generic_sleuth_span_handler() {
		BlockingDeque<SpanRecord> spanRecordQueue = new LinkedBlockingDeque<>();
		WavefrontSleuthSpanHandler handler = spanHandler(spanRecordQueue, new SimpleMeterRegistry());

		handler.end(new BraveTraceContext(TraceContext.newBuilder().traceId(1L).spanId(2L).parentId(3L).build()),
				new BraveFinishedSpan(mutableSpan()));

		SpanRecord spanRecord = takeRecord(spanRecordQueue);
		thenSpanRecordIsProperlyBuilt(spanRecord);
		thenEndSpanTimeIsGreaterThanStartTime(spanRecord);
		thenTagsAreAttached(spanRecord);
	}

	private void thenTagsAreAttached(SpanRecord spanRecord) {
		then(spanRecord.tags).containsExactlyInAnyOrder(Pair.of("application", "application"),
				Pair.of("service", "service"), Pair.of("cluster", "cluster"), Pair.of("foo", "bar"),
				Pair.of("shard", "shard"));
	}

	private void thenEndSpanTimeIsGreaterThanStartTime(SpanRecord spanRecord) {
		// spot check the unit is valid (millis not micros)
		long currentTime = System.currentTimeMillis();
		then(spanRecord.startMillis).isGreaterThan(currentTime - 5000).isLessThan(currentTime);
		// Less than a millis should round up to 1, but the test could take longer than
		// 1ms
		then(spanRecord.durationMillis).isPositive();
	}

	private void thenSpanRecordIsProperlyBuilt(SpanRecord spanRecord) {
		then(spanRecord.traceId).hasToString("00000000-0000-0000-0000-000000000001");
		then(spanRecord.parents).extracting(UUID::toString).containsExactly("00000000-0000-0000-0000-000000000003");
		then(spanRecord.followsFrom).isNull();
		// This tests that RPC spans do not share the same span ID
		then(spanRecord.spanId.toString()).isNotEqualTo("00000000-0000-0000-0000-000000000003")
				.matches("^[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}$");
		then(spanRecord.name).isEqualTo("defaultOperation");
	}

	private WavefrontSleuthSpanHandler spanHandler(BlockingDeque<SpanRecord> spanRecordQueue,
			MeterRegistry meterRegistry) {
		return new WavefrontSleuthSpanHandler(10, wavefrontSender(spanRecordQueue), meterRegistry, "source",
				applicationTags(), Collections.emptySet());
	}

	private <R> R takeRecord(BlockingDeque<R> queue) {
		R result;
		try {
			result = queue.poll(3, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		}
		then(result).withFailMessage("Record was not reported").isNotNull();
		return result;
	}

	private MutableSpan mutableSpan() {
		MutableSpan mutableSpan = new MutableSpan();
		mutableSpan.startTimestamp(System.currentTimeMillis() * 1000);
		mutableSpan.finishTimestamp(System.currentTimeMillis() * 1000);
		mutableSpan.tag("foo", "bar");
		return mutableSpan;
	}

	public ApplicationTags applicationTags() {
		String application = "application";
		String service = "service";
		ApplicationTags.Builder builder = new ApplicationTags.Builder(application, service);
		builder.cluster("cluster");
		builder.shard("shard");
		return builder.build();
	}

	private WavefrontSender wavefrontSender(BlockingDeque<SpanRecord> spanRecordQueue) {
		return new WavefrontSender() {
			@Override
			public void sendLog(String s, double v, Long aLong, String s1, Map<String, String> map) throws IOException {

			}

			@Override
			public void sendEvent(String s, long l, long l1, String s1, Map<String, String> map,
					Map<String, String> map1) throws IOException {

			}

			@Override
			public String getClientId() {
				return null;
			}

			@Override
			public void flush() {

			}

			@Override
			public int getFailureCount() {
				return 0;
			}

			@Override
			public void sendDistribution(String name, List<Pair<Double, Integer>> centroids,
					Set<HistogramGranularity> histogramGranularities, Long timestamp, String source,
					Map<String, String> tags) {

			}

			@Override
			public void sendMetric(String name, double value, Long timestamp, String source, Map<String, String> tags) {

			}

			@Override
			public void sendFormattedMetric(String point) {

			}

			@Override
			public void sendSpan(String name, long startMillis, long durationMillis, String source, UUID traceId,
					UUID spanId, List<UUID> parents, List<UUID> followsFrom, List<Pair<String, String>> tags,
					List<SpanLog> spanLogs) {
				spanRecordQueue.add(new SpanRecord(name, startMillis, durationMillis, source, traceId, spanId, parents,
						followsFrom, tags, spanLogs));
			}

			@Override
			public void close() {

			}
		};
	}

	static final class SpanRecord {

		private final String name;

		private final long startMillis;

		private final long durationMillis;

		private final String source;

		private final UUID traceId;

		private final UUID spanId;

		private final List<UUID> parents;

		private final List<UUID> followsFrom;

		private final List<Pair<String, String>> tags;

		private final List<SpanLog> spanLogs;

		SpanRecord(String name, long startMillis, long durationMillis, String source, UUID traceId, UUID spanId,
				List<UUID> parents, List<UUID> followsFrom, List<Pair<String, String>> tags, List<SpanLog> spanLogs) {
			this.name = name;
			this.startMillis = startMillis;
			this.durationMillis = durationMillis;
			this.source = source;
			this.traceId = traceId;
			this.spanId = spanId;
			this.parents = parents;
			this.followsFrom = followsFrom;
			this.tags = tags;
			this.spanLogs = spanLogs;
		}

		@Override
		public String toString() {
			return "SpanRecord{" + "name='" + name + '\'' + ", traceId=" + traceId + ", spanId=" + spanId + '}';
		}

	}

}
