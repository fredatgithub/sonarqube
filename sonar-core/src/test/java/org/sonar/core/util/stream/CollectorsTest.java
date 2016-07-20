/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.util.stream;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.sonar.core.util.stream.Collectors.index;
import static org.sonar.core.util.stream.Collectors.join;
import static org.sonar.core.util.stream.Collectors.toArrayList;
import static org.sonar.core.util.stream.Collectors.toHashSet;
import static org.sonar.core.util.stream.Collectors.toList;
import static org.sonar.core.util.stream.Collectors.toSet;
import static org.sonar.core.util.stream.Collectors.uniqueIndex;

public class CollectorsTest {

  private static final MyObj MY_OBJ_1_A = new MyObj(1, "A");
  private static final MyObj MY_OBJ_1_C = new MyObj(1, "C");
  private static final MyObj MY_OBJ_2_B = new MyObj(2, "B");
  private static final MyObj MY_OBJ_3_C = new MyObj(3, "C");
  private static final List<MyObj> SINGLE_ELEMENT_LIST = Arrays.asList(MY_OBJ_1_A);
  private static final List<MyObj> LIST_WITH_DUPLICATE_ID = Arrays.asList(MY_OBJ_1_A, MY_OBJ_2_B, MY_OBJ_1_C);
  private static final List<MyObj> LIST = Arrays.asList(MY_OBJ_1_A, MY_OBJ_2_B, MY_OBJ_3_C);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void toList_builds_an_ImmutableList() {
    List<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toList());
    assertThat(res).isInstanceOf(ImmutableList.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toList_with_size_builds_an_ImmutableList() {
    List<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toList(30));
    assertThat(res).isInstanceOf(ImmutableList.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toSet_builds_an_ImmutableSet() {
    Set<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toSet());
    assertThat(res).isInstanceOf(ImmutableSet.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toSet_with_size_builds_an_ImmutableSet() {
    Set<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toSet(30));
    assertThat(res).isInstanceOf(ImmutableSet.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toArrayList_builds_an_ArrayList() {
    List<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toArrayList());
    assertThat(res).isInstanceOf(ArrayList.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toArrayList_with_size_builds_an_ArrayList() {
    List<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toArrayList(30));
    assertThat(res).isInstanceOf(ArrayList.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toHashSet_builds_an_HashSet() {
    Set<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toHashSet());
    assertThat(res).isInstanceOf(HashSet.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void toHashSet_with_size_builds_an_ArrayList() {
    Set<Integer> res = Arrays.asList(1, 2, 3, 4, 5).stream().collect(toHashSet(30));
    assertThat(res).isInstanceOf(HashSet.class)
      .containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void uniqueIndex_empty_stream_returns_empty_map() {
    assertThat(Collections.<MyObj>emptyList().stream().collect(uniqueIndex(MyObj::getId))).isEmpty();
    assertThat(Collections.<MyObj>emptyList().stream().collect(uniqueIndex(MyObj::getId, 6))).isEmpty();
    assertThat(Collections.<MyObj>emptyList().stream().collect(uniqueIndex(MyObj::getId, MyObj::getText))).isEmpty();
    assertThat(Collections.<MyObj>emptyList().stream().collect(uniqueIndex(MyObj::getId, MyObj::getText, 10))).isEmpty();
  }

  @Test
  public void uniqueIndex_fails_when_there_is_duplicate_keys() {
    Stream<MyObj> stream = LIST_WITH_DUPLICATE_ID.stream();

    expectedDuplicateKey1IAE();

    stream.collect(uniqueIndex(MyObj::getId));
  }

  @Test
  public void uniqueIndex_with_expected_size_fails_when_there_is_duplicate_keys() {
    Stream<MyObj> stream = LIST_WITH_DUPLICATE_ID.stream();

    expectedDuplicateKey1IAE();

    stream.collect(uniqueIndex(MyObj::getId, 1));
  }

  @Test
  public void uniqueIndex_with_valueFunction_fails_when_there_is_duplicate_keys() {
    Stream<MyObj> stream = LIST_WITH_DUPLICATE_ID.stream();

    expectedDuplicateKey1IAE();

    stream.collect(uniqueIndex(MyObj::getId, MyObj::getText));
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_fails_when_there_is_duplicate_keys() {
    Stream<MyObj> stream = LIST_WITH_DUPLICATE_ID.stream();

    expectedDuplicateKey1IAE();

    stream.collect(uniqueIndex(MyObj::getId, MyObj::getText, 10));
  }

  @Test
  public void uniqueIndex_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    uniqueIndex(null);
  }

  @Test
  public void uniqueIndex_with_expected_size_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    uniqueIndex(null, 2);
  }

  @Test
  public void uniqueIndex_with_valueFunction_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    uniqueIndex(null, MyObj::getText);
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    uniqueIndex(null, MyObj::getText, 9);
  }

  @Test
  public void uniqueIndex_with_valueFunction_fails_if_value_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Value function can't be null");

    uniqueIndex(MyObj::getId, null);
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_fails_if_value_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Value function can't be null");

    uniqueIndex(MyObj::getId, null, 9);
  }

  @Test
  public void uniqueIndex_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(s -> null));
  }

  @Test
  public void uniqueIndex_with_expected_size_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(s -> null, 90));
  }

  @Test
  public void uniqueIndex_with_valueFunction_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(s -> null, MyObj::getText));
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(s -> null, MyObj::getText, 9));
  }

  @Test
  public void uniqueIndex_with_valueFunction_fails_if_value_function_returns_null() {
    expectValueFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(MyObj::getId, s -> null));
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_fails_if_value_function_returns_null() {
    expectValueFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(uniqueIndex(MyObj::getId, s -> null, 9));
  }

  @Test
  public void uniqueIndex_returns_map() {
    assertThat(LIST.stream().collect(uniqueIndex(MyObj::getId))).containsOnly(entry(1, MY_OBJ_1_A), entry(2, MY_OBJ_2_B), entry(3, MY_OBJ_3_C));
  }

  @Test
  public void uniqueIndex_with_expected_size_returns_map() {
    assertThat(LIST.stream().collect(uniqueIndex(MyObj::getId, 3))).containsOnly(entry(1, MY_OBJ_1_A), entry(2, MY_OBJ_2_B), entry(3, MY_OBJ_3_C));
  }

  @Test
  public void uniqueIndex_with_valueFunction_returns_map() {
    assertThat(LIST.stream().collect(uniqueIndex(MyObj::getId, MyObj::getText))).containsOnly(entry(1, "A"), entry(2, "B"), entry(3, "C"));
  }

  @Test
  public void uniqueIndex_with_valueFunction_and_expected_size_returns_map() {
    assertThat(LIST.stream().collect(uniqueIndex(MyObj::getId, MyObj::getText, 9))).containsOnly(entry(1, "A"), entry(2, "B"), entry(3, "C"));
  }

  @Test
  public void index_empty_stream_returns_empty_map() {
    assertThat(Collections.<MyObj>emptyList().stream().collect(index(MyObj::getId))).isEmpty();
    assertThat(Collections.<MyObj>emptyList().stream().collect(index(MyObj::getId, MyObj::getText))).isEmpty();
  }

  @Test
  public void index_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    index(null);
  }

  @Test
  public void index_with_valueFunction_fails_if_key_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't be null");

    index(null, MyObj::getText);
  }

  @Test
  public void index_with_valueFunction_fails_if_value_function_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Value function can't be null");

    index(MyObj::getId, null);
  }

  @Test
  public void index_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(index(s -> null));
  }

  @Test
  public void index_with_valueFunction_fails_if_key_function_returns_null() {
    expectKeyFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(index(s -> null, MyObj::getText));
  }

  @Test
  public void index_with_valueFunction_fails_if_value_function_returns_null() {
    expectValueFunctionCantReturnNullNPE();

    SINGLE_ELEMENT_LIST.stream().collect(index(MyObj::getId, s -> null));
  }

  @Test
  public void index_supports_duplicate_keys() {
    Multimap<Integer, MyObj> multimap = LIST_WITH_DUPLICATE_ID.stream().collect(index(MyObj::getId));

    assertThat(multimap.keySet()).containsOnly(1, 2);
    assertThat(multimap.get(1)).containsOnly(MY_OBJ_1_A, MY_OBJ_1_C);
    assertThat(multimap.get(2)).containsOnly(MY_OBJ_2_B);
  }

  @Test
  public void uniqueIndex_supports_duplicate_keys() {
    Multimap<Integer, String> multimap = LIST_WITH_DUPLICATE_ID.stream().collect(index(MyObj::getId, MyObj::getText));

    assertThat(multimap.keySet()).containsOnly(1, 2);
    assertThat(multimap.get(1)).containsOnly("A", "C");
    assertThat(multimap.get(2)).containsOnly("B");
  }

  @Test
  public void uniqueIndex_returns_multimap() {
    Multimap<Integer, MyObj> myObjImmutableListMultimap = LIST.stream().collect(index(MyObj::getId));

    assertThat(myObjImmutableListMultimap).hasSize(3);
    assertThat(myObjImmutableListMultimap).contains(entry(1, MY_OBJ_1_A), entry(2, MY_OBJ_2_B), entry(3, MY_OBJ_3_C));
  }

  @Test
  public void index_with_valueFunction_returns_map() {
    Multimap<Integer, String> multimap = LIST.stream().collect(index(MyObj::getId, MyObj::getText));

    assertThat(multimap).hasSize(3);
    assertThat(multimap).contains(entry(1, "A"), entry(2, "B"), entry(3, "C"));
  }

  @Test
  public void join_on_empty_stream_returns_empty_string() {
    assertThat(Collections.emptyList().stream().collect(join(Joiner.on(",")))).isEmpty();
  }

  @Test
  public void join_fails_with_NPE_if_joiner_is_null() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Joiner can't be null");

    join(null);
  }

  @Test
  public void join_applies_joiner_to_stream() {
    assertThat(Arrays.asList("1", "2", "3", "4").stream().collect(join(Joiner.on(","))))
      .isEqualTo("1,2,3,4");
  }

  @Test
  public void join_supports_null_if_joiner_does() {
    Stream<String> stream = Arrays.asList("1", null).stream();

    expectedException.expect(NullPointerException.class);

    stream.collect(join(Joiner.on(",")));
  }

  private void expectedDuplicateKey1IAE() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Duplicate key 1");
  }

  private void expectKeyFunctionCantReturnNullNPE() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Key function can't return null");
  }

  private void expectValueFunctionCantReturnNullNPE() {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Value function can't return null");
  }

  private static final class MyObj {
    private final int id;
    private final String text;

    public MyObj(int id, String text) {
      this.id = id;
      this.text = text;
    }

    public int getId() {
      return id;
    }

    public String getText() {
      return text;
    }
  }
}
