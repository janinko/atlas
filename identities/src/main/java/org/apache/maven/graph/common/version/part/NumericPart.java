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
package org.apache.maven.graph.common.version.part;

import java.io.Serializable;
import java.math.BigInteger;

public class NumericPart
    extends VersionPart
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    public static final NumericPart ZERO = new NumericPart( 0 );

    private final BigInteger value;

    public NumericPart( final String value )
    {
        this.value = BigInteger.valueOf( Long.parseLong( value ) );
    }

    public NumericPart( final long value )
    {
        this.value = BigInteger.valueOf( value );
    }

    @Override
    public String renderStandard()
    {
        return value.toString();
    }

    public BigInteger getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.format( "NUM[%s]", value );
    }

    public int compareTo( final VersionPart part )
    {
        // 1.2.2 > 1.2.GA, 1.2.1 > 1.2.M1
        if ( part instanceof StringPart )
        {
            // Let the StringPart compareTo(..) method do the heavy lifting.
            return -1 * ( (StringPart) part ).compareTo( this );
        }
        // 1.2.1 > 1.2-SNAPSHOT, 1.2[.0] > 1.2-SNAPSHOT
        else if ( part instanceof SnapshotPart )
        {
            return 1;
        }
        else if ( part instanceof NumericPart )
        {
            final BigInteger other = ( (NumericPart) part ).getValue();
            return value.compareTo( other );
        }

        // punt...shouldn't happen.
        return 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
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
        final NumericPart other = (NumericPart) obj;
        return value.equals( other.value );
    }

}
