/*
 * Copyright (c) 2020. Red Hat, Inc.
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
 */

package com.sun.corba.se.impl.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Class name filtering {@code Function<String, Boolean>} implementation that is configured by a {@code filterSpec}
 * string provided to the constructor. The function returns {@code true} if the given class name is acceptable
 * for class resolution, {@code false} otherwise. The function is meant to be used for implementation blacklists
 * or whitelists of classes that would be loaded when IIOP deserialization occurs.
 * <p>
 * The {@code filterSpec} string is composed of one or more filter spec elements separated by the {@code ';'} char.
 * A filter spec element that begins with the {@code '!'} char is a 'rejecting element' and indicates resolution of a
 * class name should not be allowed if the rest of the element matches the class name. Otherwise the spec element
 * indicates the class name can be accepted if it matches.
 * </p>
 * <p>
 * Matching is done according to the following rules:
 * <ul>
 *     <li>If the spec element does not terminate in the {@code '*'} char the given class name must match.</li>
 *     <li>If the spec element terminates in the string {@code ".*"} the portion of the class name up to and
 *     including any final {@code '.'} char must match. Such a spec element indicates a single package in which a class
 *     must reside.</li>
 *     <li>If the spec element terminates in the string {@code ".**"} the class name must begin with the portion of the
 *     spec element before the first {@code '*'}. Such a spec element indicates a package hierarchy in which a class
 *     must reside.</li>
 *     <li>Otherwise the spec element ends in the {@code '*'} char and the class name must begin with portion
 *     spec element before the first {@code '*'}. Such a spec element indicates a general string 'starts with' match.</li>
 * </ul>
 * </>
 * <p>
 * The presence of the {@code '='} or {@code '/'} chars anywhere in the filter spec will result in an
 * {@link IllegalArgumentException} from the constructor. The presence of the {@code '*'} char in any substring
 * other than the ones described above will also result in an {@link IllegalArgumentException} from the constructor.
 * </p>
 * <p>
 * If any element in the filter spec indicates a class name should be rejected, it will be rejected. If any element
 * in the filter spec does not begin with the {@code '!'} char, then the filter will act like a whitelist, and
 * at least one non-rejecting filter spec element must match the class name for the filter to return {@code true}.
 * Rejecting elements can be used in an overall filter spec for a whitelist, for example to exclude a particular
 * class from a package that is otherwise whitelisted.
 * </p>
 *
 * @author Brian Stansberry
 */
public final class FilterSpecClassResolverFilter implements Function<String, Boolean> {

    // Note -- the default filter spec represents a blacklist.
    /**
     * Value provided to {@link #FilterSpecClassResolverFilter(String)} by the default no-arg constructor.
     * Represents the default filtering rules for this library.
     */
    public static final String DEFAULT_FILTER_SPEC =
            "!bsh.Interpreter;"
                    + "!bsh.XThis;"
                    + "!bsh.XThis$Handler;"
                    + "!clojure.inspector.proxy$javax.swing.table.AbstractTableModel$ff19274a;"
                    + "!clojure.lang.PersistentArrayMap;"
                    + "!com.mchange.v2.naming.ReferenceIndirector$ReferenceSerialized;"
                    + "!com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;"
                    + "!com.sun.rowset.JdbcRowSetImpl;"
                    + "!com.sun.syndication.feed.impl.ObjectBean;"
                    + "!com.vaadin.data.util.NestedMethodProperty;"
                    + "!com.vaadin.data.util.PropertysetItem;"
                    + "!java.rmi.server.RemoteObject;"
                    + "!java.rmi.server.UnicastRemoteObject;"
                    + "!net.sf.json.JSONObject;"
                    + "!org.apache.commons.beanutils.BeanComparator;"
                    + "!org.apache.commons.collections.functors.InstantiateTransformer;"
                    + "!org.apache.commons.collections.functors.InvokerTransformer;"
                    + "!org.apache.commons.collections4.functors.InstantiateTransformer;"
                    + "!org.apache.commons.collections4.functors.InvokerTransformer;"
                    + "!org.apache.commons.fileupload.disk.DiskFileItem;"
                    + "!org.apache.commons.io.output.DeferredFileOutputStream;"
                    + "!org.apache.myfaces.view.facelets.el.ValueExpressionMethodExpression;"
                    + "!org.apache.wicket.util.io.DeferredFileOutputStream;"
                    + "!org.apache.wicket.util.upload.DiskFileItem;"
                    + "!org.apache.xalan.xsltc.trax.TemplatesImpl;"
                    + "!org.aspectj.weaver.tools.cache.SimpleCache$StoreableCachingMap;"
                    + "!org.codehaus.groovy.runtime.ConvertedClosure;"
                    + "!org.codehaus.groovy.runtime.MethodClosure;"
                    + "!org.hibernate.engine.spi.TypedValue;"
                    + "!org.hibernate.property.access.spi.GetterFieldImpl;"
                    + "!org.hibernate.property.access.spi.GetterMethodImpl;"
                    + "!org.hibernate.property.access.spi.SetterFieldImpl;"
                    + "!org.hibernate.property.access.spi.SetterMethodImpl;"
                    + "!org.hibernate.tuple.component.PojoComponentTuplizer;"
                    + "!org.hibernate.type.ComponentType;"
                    + "!org.jboss.as.connector.subsystems.datasources.WildFlyDataSource;"
                    + "!org.mozilla.javascript.**;"
                    + "!org.python.core.PyBytecode;"
                    + "!org.python.core.PyFunction;"
                    + "!org.python.core.PyObject;"
                    + "!org.springframework.aop.framework.AdvisedSupport;"
                    + "!org.springframework.aop.target.SingletonTargetSource;"
                    + "!org.springframework.core.SerializableTypeWrapper$*;"
                    + "!org.springframework.transaction.jta.JtaTransactionManager;"
                    + "!sun.rmi.server.ActivationGroupImpl;"
                    + "!sun.rmi.server.UnicastRef;"
                    + "!sun.rmi.server.UnicastRef2;"
                    + "!sun.rmi.server.UnicastServerRef;"
                    + "!sun.rmi.transport.LiveRef;"
                    + "!sun.rmi.transport.tcp.TCPEndpoint;"
                    // Additional gadget classes not in jdk.serialFilter because they break other use cases (e.g. JMC, JSF)
                    + "!com.mchange.v2.c3p0.PoolBackedDataSource;"
                    + "!javax.swing.UIDefaults;"
                    + "!javax.swing.UIDefaults$ProxyLazyValue;"
                    + "!javax.swing.UIDefaults$TextAndMnemonicHashMap;"
                    + "!org.springframework.beans.factory.config.MethodInvokingFactoryBean;"
                    + "!org.springframework.jndi.support.SimpleJndiBeanFactory;"
                    + "!sun.swing.SwingLazyValue";

    private final String filterSpec;
    private final List<String> parsedFilterSpecs;
    private final List<Function<String, Boolean>> unmarshallingFilters;
    private final boolean whitelistUnmarshalling;

    /**
     * Creates a filter using the default rules.
     */
    public FilterSpecClassResolverFilter() {
        this(getDeserializationFilterSpec());
    }


    private static String getDeserializationFilterSpec() {
        // The default blacklisting can be disabled via system property
        String disabled = System.getProperty("jboss.iiop.deserialization.filter.disabled");
        if ("true".equalsIgnoreCase(disabled)) {
            return "";  // empty string disables filtering
        }
        // Allow external specification of the filter spec. This config mechanism may change incompatibly
        // in a future major or minor release. Note that this follows JEP 290 configuration pattern.
        String spec = System.getProperty("jboss.experimental.iiop.deserialization.filter.spec");
        if (spec != null) {
            return spec;
        }
        return DEFAULT_FILTER_SPEC;
    }

    /**
     * Create a filter using the given {@code filterSpec}.
     * @param filterSpec filter configuration as described in the class javadoc. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if the form of {@code filterSpec} violates any of the rules for this class
     */
    public FilterSpecClassResolverFilter(String filterSpec) {
        if (filterSpec == null) {
            throw new IllegalArgumentException("filterSpec cannot be null");
        }
        this.filterSpec = filterSpec;
        if (filterSpec.isEmpty()) {
            parsedFilterSpecs = null;
            unmarshallingFilters = null;
            whitelistUnmarshalling = false;
        } else {

            parsedFilterSpecs = new ArrayList<>(Arrays.asList(filterSpec.split(";")));
            unmarshallingFilters = new ArrayList<>(parsedFilterSpecs.size());
            ExactMatchFilter exactMatchWhitelist = null;
            ExactMatchFilter exactMatchBlacklist = null;
            boolean whitelist = false;

            for (String spec : parsedFilterSpecs) {

                if (spec.contains("=") || spec.contains("/")) {
                    // perhaps this is an attempt to pass a JEPS 290 style limit or module name pattern; not supported
                    throw new IllegalArgumentException("Invalid filter spec: " + spec);
                }

                boolean blacklistElement = spec.startsWith("!");
                whitelist |= !blacklistElement;

                // For a blacklist element, return FALSE for a match; i.e. don't resolve
                // For a whitelist, return TRUE for a match; i.e. definitely do resolve
                // For any non-match, return null which means that check has no opinion
                final Boolean matchReturn = blacklistElement ? Boolean.FALSE : Boolean.TRUE;

                if (blacklistElement) {
                    if (spec.length() == 1) {
                        throw new IllegalArgumentException("Invalid filter spec: " + spec);
                    }
                    spec = spec.substring(1);
                }

                Function<String, Boolean> filter = null;
                int lastStar = spec.lastIndexOf('*');
                if (lastStar >= 0) {
                    if (lastStar != spec.length() - 1) {
                        // wildcards only allowed at the end
                        throw new IllegalArgumentException("Invalid filter spec: " + spec);
                    }
                    int firstStar = spec.indexOf('*');
                    if (firstStar != lastStar) {
                        if (firstStar == lastStar - 1 && spec.endsWith(".**")) {
                            if (spec.length() == 3) {
                                throw new IllegalArgumentException("Invalid filter spec: " + spec);
                            }
                            String pkg = spec.substring(0, spec.length() - 2);
                            filter = cName -> cName.startsWith(pkg) ? matchReturn : null;
                        } else {
                            // there's an extra star in some spot other than between a final '.' and '*'
                            throw new IllegalArgumentException("Invalid filter spec: " + spec);
                        }
                    } else if (spec.endsWith(".*")) {
                        if (spec.length() == 2) {
                            throw new IllegalArgumentException("Invalid filter spec: " + spec);
                        }
                        String pkg = spec.substring(0, spec.length() - 1);
                        filter = cName -> cName.startsWith(pkg) && cName.lastIndexOf('.') == pkg.length() - 1 ? matchReturn : null;
                    } else {
                        String startsWith = spec.substring(0, spec.length() - 1); // note that an empty 'startsWith' is ok; e.g. from a "*" spec to allow all
                        filter = cName -> cName.startsWith(startsWith) ? matchReturn : null;
                    }
                } else {
                    // For exact matches store them in a set and just do a single set.contains check
                    if (blacklistElement) {
                        if (exactMatchBlacklist == null) {
                            filter = exactMatchBlacklist = new ExactMatchFilter(false);
                        }
                        exactMatchBlacklist.addMatchingClass(spec);
                    } else {
                        if (exactMatchWhitelist == null) {
                            filter = exactMatchWhitelist = new ExactMatchFilter(true);
                        }
                        exactMatchWhitelist.addMatchingClass(spec);
                    }
                     if (filter == null) {
                         // An ExactMatchFilter earlier in the list would have already handled this.
                         // Just add a no-op placeholder function to keep the list of specs and functions in sync
                         filter = cName -> null;
                     }
                }
                unmarshallingFilters.add(filter);
            }
            if (whitelist) {
                // Don't force users to whitelist the classes we send. Add a whitelist spec for their package
                // TODO is this a good idea?
                final String pkg = "org.jboss.ejb.client.";
                parsedFilterSpecs.add(pkg + "*");
                unmarshallingFilters.add(cName -> cName.startsWith(pkg) && cName.lastIndexOf('.') == pkg.length() - 1 ? true : null);
            }
            assert parsedFilterSpecs.size() == unmarshallingFilters.size();
            whitelistUnmarshalling = whitelist;
        }
    }

    @Override
    public Boolean apply(String className) {
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        boolean anyAccept = false;
        if (unmarshallingFilters != null) {

            for (int i = 0; i < unmarshallingFilters.size(); i++) {
                Function<String, Boolean> func = unmarshallingFilters.get(i);
                Boolean accept = func.apply(className);
                if (accept == Boolean.FALSE) {
                    String failedSpec = func instanceof ExactMatchFilter ? "!" + className : parsedFilterSpecs.get(i);
                    // Class explicitly rejected by filter spec
                    return false;
                } else {
                    anyAccept |= accept != null;
                }
            }
            if (whitelistUnmarshalling && !anyAccept) {
                // Class not explicitly whitelisted by filter spec
                return false;
            }
        }
        return true;
    }

    private static class ExactMatchFilter implements Function<String, Boolean> {
        private final Set<String> matches = new HashSet<>();
        private final Boolean matchResult;

        private ExactMatchFilter(boolean forWhitelist) {
            this.matchResult = forWhitelist;
        }

        private void addMatchingClass(String name) {
            matches.add(name);
        }

        @Override
        public Boolean apply(String s) {
            return matches.contains(s) ? matchResult : null;
        }
    }
}
