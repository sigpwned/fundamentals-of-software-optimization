/*-
 * =================================LICENSE_START==================================
 * emoji4j-core
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
package com.sigpwned.foso.sut.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.sigpwned.foso.sut.GraphemeData;
import com.sigpwned.foso.sut.trie.DefaultGraphemeTrie;

public final class Graphemes {
  private Graphemes() {}

  public static GraphemeData getGraphemeData() {
    JSONObject o;
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("graphemes.json")) {
      o = new JSONObject(new JSONTokener(in));
    } catch (IOException e) {
      throw new UncheckedIOException("failed to load grapheme data", e);
    }
    return Serialization.deserializeGraphemeData(o);
  }

  private static DefaultGraphemeTrie defaultTrie;

  public static synchronized DefaultGraphemeTrie getDefaultTrie() {
    if (defaultTrie == null)
      defaultTrie = DefaultGraphemeTrie.fromGraphemeData(getGraphemeData());
    return defaultTrie;
  }
}
