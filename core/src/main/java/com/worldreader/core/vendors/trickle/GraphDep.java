/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.worldreader.core.vendors.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

class GraphDep<T> implements Dep<T> {

  private final Graph<T> graph;

  public GraphDep(Graph<T> graph) {
    checkNotNull(graph, "graph");
    this.graph = graph;
  }

  @Override public ListenableFuture<T> getFuture(TraverseState state) {
    return state.futureForGraph(graph);
  }

  @Override public NodeInfo getNodeInfo() {
    return graph;
  }
}
