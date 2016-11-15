/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.client.routing;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.common.util.SafeCloseable;

/**
 * A list of {@link Endpoint}s.
 */
@FunctionalInterface
public interface EndpointGroup extends SafeCloseable {
    /**
     * Return the endpoints held by this {@link EndpointGroup}.
     */
    List<Endpoint> endpoints();

    @Override
    default void close() {}

    /*
     * Creates a new {@link EndpointGroup} that tries this {@link EndpointGroup} first and then the specified
     * {@link EndpointGroup} when this {@link EndpointGroup} does not have a requested resource.
     *
     * @param nextEndpointGroup the {@link EndpointGroup} to try secondly.
     */
    default EndpointGroup orElse(EndpointGroup nextEndpointGroup) {
        return new OrElseEndpointGroup(this, nextEndpointGroup);
    }

    final class OrElseEndpointGroup implements EndpointGroup {

        private final EndpointGroup first;
        private final EndpointGroup second;

        OrElseEndpointGroup(EndpointGroup first, EndpointGroup second) {
            this.first = requireNonNull(first, "first");
            this.second = requireNonNull(second, "second");
        }

        @Override
        public List<Endpoint> endpoints() {
            List<Endpoint> endpoints = first.endpoints();
            if (!endpoints.isEmpty()) {
                return endpoints;
            }
            return second.endpoints();
        }
    }
}
