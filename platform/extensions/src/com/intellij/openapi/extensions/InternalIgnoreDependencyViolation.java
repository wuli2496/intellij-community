// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow class to be used among various plugins.
 * By default classloader of extension class must be equal to classloader of plugin where extension is defined.
 * <p/>
 * Generally, IDEA prohibits referencing extension class belonging to {@code plugin1} from the {@code plugin.xml} in {@code plugin2}.
 * However, sometimes this kind of dependency violation is necessary, in which case the corresponding extension must be annotated with this annotation.
 * <p/>
 * For example:
 * <p/>
 * {@code plugin.xml:}
 * <pre>{@code
 * <findUsageHandler implementation="com.plugin1.MyHandler"/>}</pre>
 * <p/>
 * {@code MyHandler.java:}<pre>{@code
 * @InternalIgnoreDependencyViolation
 * class MyHandler {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InternalIgnoreDependencyViolation {
}
