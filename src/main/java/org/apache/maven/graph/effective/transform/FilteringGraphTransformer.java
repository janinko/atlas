/*******************************************************************************
 * Copyright 2012 John Casey
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.maven.graph.effective.transform;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.graph.effective.EProjectCycle;
import org.apache.maven.graph.effective.EProjectGraph;
import org.apache.maven.graph.effective.EProjectNet;
import org.apache.maven.graph.effective.EProjectRelationships;
import org.apache.maven.graph.effective.filter.ProjectRelationshipFilter;
import org.apache.maven.graph.effective.ref.EProjectKey;
import org.apache.maven.graph.effective.rel.ProjectRelationship;
import org.apache.maven.graph.effective.traverse.AbstractFilteringTraversal;

public class FilteringGraphTransformer
    extends AbstractFilteringTraversal
    implements ProjectGraphTransformer
{

    private EProjectKey key;

    private final Set<ProjectRelationship<?>> relationships = new HashSet<ProjectRelationship<?>>();

    private Set<EProjectCycle> cycles;

    public FilteringGraphTransformer( final ProjectRelationshipFilter filter )
    {
        super( filter );
    }

    public EProjectGraph getTransformedGraph()
    {
        for ( final Iterator<EProjectCycle> iterator = cycles.iterator(); iterator.hasNext(); )
        {
            final EProjectCycle cycle = iterator.next();

            for ( final ProjectRelationship<?> rel : cycle )
            {
                if ( !relationships.contains( rel ) )
                {
                    iterator.remove();
                }
            }
        }

        return new EProjectGraph( key, relationships, Collections.<EProjectRelationships> emptyList(), cycles );
    }

    @Override
    protected boolean shouldTraverseEdge( final ProjectRelationship<?> relationship,
                                          final List<ProjectRelationship<?>> path, final int pass )
    {
        relationships.add( relationship );
        return true;
    }

    @Override
    public void startTraverse( final int pass, final EProjectNet network )
    {
        if ( key == null )
        {
            if ( !( network instanceof EProjectGraph ) )
            {
                throw new IllegalArgumentException( "Only networks of type " + EProjectGraph.class.getSimpleName()
                    + " are transformable currently." );
            }

            final EProjectGraph graph = (EProjectGraph) network;
            this.key = graph.getKey();
            this.cycles = new HashSet<EProjectCycle>();

            final Set<EProjectCycle> graphCycles = graph.getCycles();
            if ( graphCycles != null && !graphCycles.isEmpty() )
            {
                this.cycles.addAll( graphCycles );
            }
        }

        super.startTraverse( pass, network );
    }

}
