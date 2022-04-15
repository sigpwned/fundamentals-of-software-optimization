/*-
 * =================================LICENSE_START==================================
 * emoji4j-benchmarks
 * ====================================SECTION=====================================
 * Copyright (C) 2022 Andy Boothe
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package com.sigpwned.foso.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sigpwned.foso.sut.GraphemeMatcher;
import com.sigpwned.foso.sut.trie.DefaultGraphemeTrie;
import com.sigpwned.foso.sut.util.Graphemes;

@Fork(value = 3) // Run 3 executions in different processes
@Warmup(iterations = 5) // In each fork, run 5 iterations to warm up
@Measurement(iterations = 5) // In each fork, run 5 iterations to measure
@OutputTimeUnit(TimeUnit.SECONDS) // Report the output time in seconds
@BenchmarkMode(Mode.Throughput) // Our metric is throughput
@State(Scope.Benchmark) // The initialization covers a whole fork
public class GraphemeMatcherBenchmark {
  /**
   * Contains exactly 1MB of "random" data sampled from Twitter streaming API. Visually confirmed to
   * be emoji-rich.
   */
  public String tweets;

  /**
   * The data structure to use to scan for emoji
   */
  public DefaultGraphemeTrie trie;

  @Setup
  public void setupGraphemeMatcherBenchmark() throws IOException {
    // Load the text to process during our benchmark
    try (
        InputStream in = new GZIPInputStream(Resources.getResource("tweets.txt.gz").openStream())) {
      tweets = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
    }

    // Build the data structure we need to scan for emoji
    trie = DefaultGraphemeTrie.fromGraphemeData(Graphemes.getGraphemeData());
  }

  /**
   * The benchmark workload
   */
  @Benchmark
  public void tweets(Blackhole blackhole) {
    // Let's count our matches
    int count = 0;

    // For each emoji match, increment our count
    GraphemeMatcher m = new GraphemeMatcher(trie, tweets);
    while (m.find()) {
      count = count + 1;
    }

    // In Java, if work happens that does not result in externally-visible side
    // effects, then the JIT can optimize it out. JMH provides the Blackhole,
    // which can be used to generate a side effect that prevents the benchmark
    // from being optimized away. This guarantees we're measuring what we think
    // we're measuring.
    blackhole.consume(count);
  }
}
