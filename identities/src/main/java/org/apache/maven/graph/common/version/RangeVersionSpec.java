/*******************************************************************************
 * Copyright (C) 2013 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.apache.maven.graph.common.version;

import java.io.Serializable;

public class RangeVersionSpec
    implements VersionSpec, Serializable, MultiVersionSpec
{

    private static final long serialVersionUID = 1L;

    private final SingleVersion lower;

    private final SingleVersion upper;

    private final boolean lowerInclusive;

    private final boolean upperInclusive;

    private final boolean snapshotAllowed;

    private final String rawExpression;

    public RangeVersionSpec( final String rawExpression, final SingleVersion lower, final SingleVersion upper,
                             final boolean lowerInclusive, final boolean upperInclusive )
    {
        this.rawExpression = rawExpression;
        if ( lower == null && upper == null )
        {
            throw new IllegalArgumentException(
                                                "You MUST supply at least a lower- or upper-bound version to have a valid range!" );
        }

        this.lower = lower;
        this.upper = upper;
        this.lowerInclusive = lowerInclusive;
        this.upperInclusive = upperInclusive;

        snapshotAllowed = ( lower != null && lower.isSnapshot() ) || ( upper != null && upper.isSnapshot() );
    }

    public boolean isPinned()
    {
        return lowerInclusive && upperInclusive && lower != null && lower.equals( upper );
    }

    public SingleVersion getPinnedVersion()
    {
        return isPinned() ? lower : null;
    }

    public boolean isSnapshot()
    {
        return snapshotAllowed;
    }

    public boolean isRelease()
    {
        return !isSnapshot();
    }

    public String renderStandard()
    {
        return rawExpression == null ? String.format( "%s%s,%s%s", ( lowerInclusive ? "[" : "(" ), ( lower == null ? ""
                                                                      : lower.renderStandard() ),
                                                      ( upperInclusive ? "]" : ")" ),
                                                      ( upper == null ? "" : upper.renderStandard() ) ) : rawExpression;
    }

    public boolean contains( final VersionSpec version )
    {
        if ( version == null )
        {
            return false;
        }

        if ( version instanceof SingleVersion )
        {
            return containsSingle( (SingleVersion) version );
        }
        else if ( version instanceof RangeVersionSpec )
        {
            return containsRange( (RangeVersionSpec) version );
        }
        else
        {
            for ( final VersionSpec spec : ( (CompoundVersionSpec) version ) )
            {
                if ( !contains( spec ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean containsRange( final RangeVersionSpec version )
    {
        final SingleVersion oLower = version.getLowerBound();
        final SingleVersion oUpper = version.getUpperBound();

        if ( lower != null )
        {
            if ( oLower == null )
            {
                return false;
            }
            else
            {
                final int comp = VersionSpecComparisons.compareTo( lower, version );
                if ( comp > 0 || ( comp == 0 && lowerInclusive && !version.isLowerBoundInclusive() ) )
                {
                    return false;
                }
            }
        }

        if ( upper != null )
        {
            if ( oUpper == null )
            {
                return false;
            }
            else
            {
                final int comp = VersionSpecComparisons.compareTo( upper, version );
                if ( comp < 0 || ( comp == 0 && upperInclusive && !version.isUpperBoundInclusive() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean containsSingle( final SingleVersion version )
    {
        if ( lower != null )
        {
            if ( lowerInclusive && !lower.isRelease() && !version.isRelease() )
            {
                return true;
            }

            final int comp = VersionSpecComparisons.compareTo( lower, version );
            if ( comp > 0 || ( comp == 0 && !lowerInclusive ) )
            {
                return false;
            }
        }

        if ( upper != null )
        {
            if ( upperInclusive && !upper.isRelease() && !version.isRelease() )
            {
                return true;
            }

            final int comp = VersionSpecComparisons.compareTo( upper, version );
            if ( comp < 0 || ( comp == 0 && !upperInclusive ) )
            {
                return false;
            }
        }

        return true;
    }

    public int compareTo( final VersionSpec other )
    {
        return VersionSpecComparisons.compareTo( this, other );
    }

    @Override
    public String toString()
    {
        return String.format( "Range [lower=%s, lowerInclusive=%s, upper=%s, upperInclusive=%s, snapshots allowed? %s] (%s)",
                              lower, lowerInclusive, upper, upperInclusive, snapshotAllowed, renderStandard() );
    }

    public SingleVersion getLowerBound()
    {
        return lower;
    }

    public SingleVersion getUpperBound()
    {
        return upper;
    }

    public boolean isLowerBoundInclusive()
    {
        return lowerInclusive;
    }

    public boolean isUpperBoundInclusive()
    {
        return upperInclusive;
    }

    public boolean isConcrete()
    {
        return isPinned() && lower.isConcrete();
    }

    public boolean isSingle()
    {
        return isPinned();
    }

    public SingleVersion getConcreteVersion()
    {
        return isConcrete() ? lower.getConcreteVersion() : null;
    }

    public SingleVersion getSingleVersion()
    {
        return isSingle() ? lower.getSingleVersion() : null;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( lower == null ) ? 0 : lower.hashCode() );
        result = prime * result + ( lowerInclusive ? 1231 : 1237 );
        result = prime * result + ( snapshotAllowed ? 1231 : 1237 );
        result = prime * result + ( ( upper == null ) ? 0 : upper.hashCode() );
        result = prime * result + ( upperInclusive ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final RangeVersionSpec other = (RangeVersionSpec) obj;
        if ( lower == null )
        {
            if ( other.lower != null )
            {
                return false;
            }
        }
        else if ( !lower.equals( other.lower ) )
        {
            return false;
        }
        if ( lowerInclusive != other.lowerInclusive )
        {
            return false;
        }
        if ( snapshotAllowed != other.snapshotAllowed )
        {
            return false;
        }
        if ( upper == null )
        {
            if ( other.upper != null )
            {
                return false;
            }
        }
        else if ( !upper.equals( other.upper ) )
        {
            return false;
        }
        if ( upperInclusive != other.upperInclusive )
        {
            return false;
        }
        return true;
    }

}
