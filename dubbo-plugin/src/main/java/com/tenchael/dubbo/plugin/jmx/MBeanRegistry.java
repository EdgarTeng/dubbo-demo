/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenchael.dubbo.plugin.jmx;

import com.tenchael.dubbo.plugin.utils.SwallowedExceptionListener;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class MBeanRegistry {

    private static final String DEFAULT_JMX_NAME_PREFIX = "metrics";

    private static volatile MBeanRegistry instance = new MBeanRegistry();

    private MBeanServer mBeanServer;

    private SwallowedExceptionListener swallowedExceptionListener;

    /**
     * Useful for unit tests. Change the MBeanRegistry instance
     *
     * @param instance new instance
     */
    public static void setInstance(MBeanRegistry instance) {
        MBeanRegistry.instance = instance;
    }

    public static MBeanRegistry getInstance() {
        return instance;
    }

    public MBeanRegistry() {
        try {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (Error e) {
            // Account for running within IKVM and create a new MBeanServer
            // if the PlatformMBeanServer does not exist.
            mBeanServer = MBeanServerFactory.createMBeanServer();
        }
    }

    /**
     * Return the underlying MBeanServer that is being
     * used to register MBean's. The returned MBeanServer
     * may be a new empty MBeanServer if running through IKVM.
     */
    public MBeanServer getPlatformMBeanServer() {
        return mBeanServer;
    }


    public ObjectName register(String nameBase, String namePrefix, MetricsMXBean mBean) {
        ObjectName objectName = null;
        MBeanServer mbs = getPlatformMBeanServer();
        int i = 1;
        boolean registered = false;
        String base = nameBase;
        while (!registered) {
            try {
                ObjectName objName;
                // Skip the numeric suffix for the first pool in case there is
                // only one so the names are cleaner.
                if (i == 1) {
                    objName = new ObjectName(base + namePrefix);
                } else {
                    objName = new ObjectName(base + namePrefix + i);
                }
                mbs.registerMBean(mBean, objName);
                objectName = objName;
                registered = true;
            } catch (MalformedObjectNameException e) {
                // Must be an invalid name. Use the defaults instead.
                swallowException(e);
                namePrefix = DEFAULT_JMX_NAME_PREFIX;
                base = nameBase;
            } catch (InstanceAlreadyExistsException e) {
                // Increment the index and try again
                swallowException(e);
                i++;
            } catch (MBeanRegistrationException e) {
                // Shouldn't happen. Skip registration if it does.
                swallowException(e);
                registered = true;
            } catch (NotCompliantMBeanException e) {
                // Shouldn't happen. Skip registration if it does.
                swallowException(e);
                registered = true;
            }
        }
        return objectName;
    }

    public void unregister(ObjectName oname) {
        if (oname != null) {
            try {
                getPlatformMBeanServer().unregisterMBean(oname);
            } catch (MBeanRegistrationException e) {
                swallowException(e);
            } catch (InstanceNotFoundException e) {
                swallowException(e);
            }
        }
    }

    final void swallowException(Exception e) {
        SwallowedExceptionListener listener = getSwallowedExceptionListener();

        if (listener == null) {
            return;
        }

        try {
            listener.onSwallowException(e);
        } catch (OutOfMemoryError oome) {
            throw oome;
        } catch (VirtualMachineError vme) {
            throw vme;
        } catch (Throwable t) {
            // Ignore. Enjoy the irony.
        }
    }

    public final SwallowedExceptionListener getSwallowedExceptionListener() {
        return swallowedExceptionListener;
    }

    public final void setSwallowedExceptionListener(SwallowedExceptionListener swallowedExceptionListener) {
        this.swallowedExceptionListener = swallowedExceptionListener;
    }


}
