/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.tinkergraph.structure.specialized.gratefuldead;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.SpecializedElementFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.SpecializedTinkerVertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerVertexProperty;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.*;

public class Song extends SpecializedTinkerVertex<String> {
    public static final String label = "song";

    public static final String NAME = "name";
    public static final String SONG_TYPE = "songType";
    public static final String PERFORMANCES = "performances";
    public static final Set<String> SPECIFIC_KEYS = new HashSet<>(Arrays.asList(NAME, SONG_TYPE, PERFORMANCES));

    // properties
    private String name;
    private String songType;
    private Integer performances;

    // edges
    public static final String[] ALL_EDGES = new String[] {FollowedBy.label, WrittenBy.label, SungBy.label};
    private Set<FollowedBy> followedByOut;
    private Set<FollowedBy> followedByIn;
    private Set<WrittenBy> writtenByOut;
    private Set<SungBy> sungByOut;

    public Song(String id, TinkerGraph graph) {
        super(id, Song.label, graph, SPECIFIC_KEYS);
    }

    /* note: usage of `==` (pointer comparison) over `.equals` (String content comparison) is intentional for performance - use the statically defined strings */
    @Override
    protected <V> Iterator<VertexProperty<V>> specificProperties(String key) {
        final VertexProperty<V> ret;
        if (key == NAME && name != null) {
            ret = new TinkerVertexProperty(this, key, name);
        } else if (key == SONG_TYPE && songType != null) {
            ret = new TinkerVertexProperty(this, key, songType);
        } else if (key == PERFORMANCES && performances != null) {
            ret = new TinkerVertexProperty(this, key, performances);
        } else {
            ret = VertexProperty.empty();
        }
        return IteratorUtils.of(ret);
    }

    @Override
    protected <V> VertexProperty<V> updateSpecificProperty(
      VertexProperty.Cardinality cardinality, String key, V value) {
        if (NAME.equals(key)) {
            this.name = (String) value;
        } else if (SONG_TYPE.equals(key)) {
            this.songType = (String) value;
        } else if (PERFORMANCES.equals(key)) {
            this.performances = (Integer) value;
        } else {
            throw new RuntimeException("property with key=" + key + " not (yet) supported by " + this.getClass().getName());
        }
        return property(key);
    }

    /* note: usage of `==` (pointer comparison) over `.equals` (String content comparison) is intentional for performance - use the statically defined strings */
    @Override
    protected Iterator<Edge> specificEdges(Direction direction, String... edgeLabels) {
        List<Iterator<?>> iterators = new LinkedList<>();
        if (edgeLabels.length == 0) {
            edgeLabels = ALL_EDGES;
        }
        for (String label : edgeLabels) {
            if (label == FollowedBy.label) {
                if (direction == Direction.IN || direction == Direction.BOTH) {
                    iterators.add(getFollowedByIn().iterator());
                }
                if (direction == Direction.OUT || direction == Direction.BOTH) {
                    iterators.add(getFollowedByOut().iterator());
                }
            } else if (label == WrittenBy.label) {
                if (direction == Direction.OUT) {
                    iterators.add(getWrittenByOut().iterator());
                }
            } else if (label == SungBy.label) {
                if (direction == Direction.OUT) {
                    iterators.add(getSungByOut().iterator());
                }
            }
        }

        Iterator<Edge>[] iteratorsArray = iterators.toArray(new Iterator[iterators.size()]);
        return IteratorUtils.concat(iteratorsArray);
    }

    @Override
    protected void removeSpecificOutEdge(Edge edge) {
        if (edge instanceof FollowedBy) {
            if (followedByOut != null) {
                followedByOut.remove(edge);
            }
        } else if (edge instanceof WrittenBy) {
            if (writtenByOut != null) {
                writtenByOut.remove(edge);
            }
        } else if (edge instanceof SungBy) {
            if (sungByOut != null) {
                sungByOut.remove(edge);
            }
        } else {
            throw new IllegalArgumentException("edge type " + edge.getClass() + " not supported");
        }

    }

    @Override
    protected void removeSpecificInEdge(Edge edge) {
        if (edge instanceof FollowedBy) {
            if (followedByIn != null) {
                followedByIn.remove(edge);
            }
        } else {
            throw new IllegalArgumentException("edge type " + edge.getClass() + " not supported");
        }
    }

    @Override
    protected void addSpecializedOutEdge(Edge edge) {
        if (edge instanceof FollowedBy) {
            getFollowedByOut().add((FollowedBy) edge);
        } else if (edge instanceof WrittenBy) {
            getWrittenByOut().add((WrittenBy) edge);
        } else if (edge instanceof SungBy) {
            getSungByOut().add((SungBy) edge);
        } else {
            throw new IllegalArgumentException("edge type " + edge.getClass() + " not supported");
        }
    }

    @Override
    protected void addSpecializedInEdge(Edge edge) {
        if (edge instanceof FollowedBy) {
            getFollowedByIn().add((FollowedBy) edge);
        } else {
            throw new IllegalArgumentException("edge type " + edge.getClass() + " not supported");
        }
    }

    private Set<FollowedBy> getFollowedByOut() {
        if (followedByOut == null) {
            followedByOut = new HashSet<>();
        }
        return followedByOut;
    }

    private Set<FollowedBy> getFollowedByIn() {
        if (followedByIn == null) {
            followedByIn = new HashSet<>();
        }
        return followedByIn;
    }

    private Set<WrittenBy> getWrittenByOut() {
        if (writtenByOut == null) {
            writtenByOut = new HashSet<>();
        }
        return writtenByOut;
    }
    private Set<SungBy> getSungByOut() {
        if (sungByOut == null) {
            sungByOut = new HashSet<>();
        }
        return sungByOut;
    }

    public static SpecializedElementFactory.ForVertex<Song, String> factory = new SpecializedElementFactory.ForVertex<Song, String>() {
        @Override
        public String forLabel() {
            return Song.label;
        }

        @Override
        public Song createVertex(String id, TinkerGraph graph) {
            return new Song(id, graph);
        }
    };
}
