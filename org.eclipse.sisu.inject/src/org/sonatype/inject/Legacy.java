/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.sonatype.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

import javax.inject.Inject;

import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.Logs;

import com.google.inject.Key;
import com.google.inject.Provider;

@Deprecated
public final class Legacy<S>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Legacy<org.eclipse.sisu.BeanEntry<?, ?>> LEGACY_BEAN_ENTRY = Legacy.as( BeanEntry.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Constructor<?> proxyConstructor;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Legacy( final Class<? extends S> clazz )
    {
        Constructor<?> ctor = null;
        try
        {
            final Class<?> proxyClazz = Proxy.getProxyClass( clazz.getClassLoader(), clazz );
            ctor = proxyClazz.getConstructor( InvocationHandler.class );
        }
        catch ( final Throwable e )
        {
            Logs.throwUnchecked( e );
        }
        this.proxyConstructor = ctor;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public <T extends S> T proxy( final S delegate )
    {
        try
        {
            return null == delegate ? null : (T) proxyConstructor.newInstance( new InvocationHandler()
            {
                public Object invoke( final Object proxy, final Method method, final Object[] args )
                    throws Throwable
                {
                    return method.invoke( delegate, args );
                }
            } );
        }
        catch ( final Throwable e )
        {
            Logs.throwUnchecked( e );
            return null; // not used
        }
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static <S, T extends S> Legacy<S> as( final Class<T> clazz )
    {
        return new Legacy<S>( clazz );
    }

    public static <Q extends Annotation, T> BeanEntry<Q, T> adapt( final org.eclipse.sisu.BeanEntry<Q, T> delegate )
    {
        return LEGACY_BEAN_ENTRY.proxy( delegate );
    }

    public static <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> adapt( final Iterable<org.eclipse.sisu.BeanEntry<Q, T>> delegate )
    {
        return new Iterable<BeanEntry<Q, T>>()
        {
            public Iterator<BeanEntry<Q, T>> iterator()
            {
                final Iterator<org.eclipse.sisu.BeanEntry<Q, T>> itr = delegate.iterator();
                return new Iterator<BeanEntry<Q, T>>()
                {
                    public boolean hasNext()
                    {
                        return itr.hasNext();
                    }

                    public BeanEntry<Q, T> next()
                    {
                        return Legacy.adapt( itr.next() );
                    }

                    public void remove()
                    {
                        itr.remove();
                    }
                };
            }
        };
    }

    public static <Q extends Annotation, T, W> org.eclipse.sisu.Mediator<Q, T, W> adapt( final Mediator<Q, T, W> delegate )
    {
        return null == delegate ? null : new org.eclipse.sisu.Mediator<Q, T, W>()
        {
            public void add( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W watcher )
                throws Exception
            {
                delegate.add( Legacy.adapt( entry ), watcher );
            }

            public void remove( final org.eclipse.sisu.BeanEntry<Q, T> entry, final W watcher )
                throws Exception
            {
                delegate.remove( Legacy.adapt( entry ), watcher );
            }
        };
    }

    public static <K extends Annotation, V> Provider<Iterable<BeanEntry<K, V>>> beanEntriesProvider( final Key<V> key )
    {
        return new Provider<Iterable<BeanEntry<K, V>>>()
        {
            @Inject
            private BeanLocator locator;

            public Iterable<BeanEntry<K, V>> get()
            {
                return Legacy.adapt( locator.<K, V> locate( key ) );
            }
        };
    }
}
