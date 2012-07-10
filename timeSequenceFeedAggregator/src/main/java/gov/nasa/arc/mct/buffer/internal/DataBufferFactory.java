/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.buffer.internal;

import gov.nasa.arc.mct.buffer.config.FastDiskBufferEnv;
import gov.nasa.arc.mct.buffer.config.MemoryBufferEnv;
import gov.nasa.arc.mct.buffer.config.DiskBufferEnv;
import gov.nasa.arc.mct.buffer.disk.internal.FastDiskDataBufferHelper;
import gov.nasa.arc.mct.buffer.memory.internal.MemoryDataBufferHelper;
import gov.nasa.arc.mct.buffer.disk.internal.NonCODDiskDataBufferHelper;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataBufferFactory {
    private final static AtomicBoolean fastDiskBufferInitializeToken = new AtomicBoolean(false);
    private final static AtomicBoolean memoryBufferInitializeToken = new AtomicBoolean(false);
    private final static AtomicBoolean nonCODDiskBufferInitializeToken = new AtomicBoolean(false);
    private static volatile DataBuffer fastDiskDataBuffer;
    private static volatile DataBuffer memoryDataBuffer;
    private static volatile DataBuffer nonCODDiskDataBuffer;
    private final static DataBufferHelper fastDiskBufferHelper = new FastDiskDataBufferHelper();
    private final static DataBufferHelper memoryBufferHelper = new MemoryDataBufferHelper();
    private final static DataBufferHelper nonCODDiskBufferHelper = new NonCODDiskDataBufferHelper();

    private DataBufferFactory() {
        //
    }

    public static DataBuffer getMemoryDataBuffer(Properties prop) {
        if (!memoryBufferInitializeToken.get()) {
            synchronized(DataBufferFactory.class) {
                if (memoryDataBuffer == null) {
                    memoryDataBuffer = new CODataBuffer(new MemoryBufferEnv(prop), memoryBufferHelper);
                }
            }
            memoryBufferInitializeToken.compareAndSet(false, true);
        }
        return memoryDataBuffer;
    }

    public static DataBuffer getFastDiskDataBuffer(Properties prop) {
        if (!fastDiskBufferInitializeToken.get()) {
            synchronized(DataBufferFactory.class) {
                if (fastDiskDataBuffer == null) {
                    fastDiskDataBuffer = new CODataBuffer(new FastDiskBufferEnv(prop), fastDiskBufferHelper);
                }
            }
            fastDiskBufferInitializeToken.compareAndSet(false, true);
        }
        return fastDiskDataBuffer;
    }
    
    public static DataBuffer getNonCODDiskDataBuffer(Properties prop) {
        if (!nonCODDiskBufferInitializeToken.get()) {
            synchronized(DataBufferFactory.class) {
                if (nonCODDiskDataBuffer == null) {
                    nonCODDiskDataBuffer = new NonCODataBuffer(new DiskBufferEnv(prop), nonCODDiskBufferHelper);
                }
            }
            nonCODDiskBufferInitializeToken.compareAndSet(false, true);
        }
        return nonCODDiskDataBuffer;
    }

    static void reset() {
        fastDiskDataBuffer = null;
        memoryDataBuffer = null;
        nonCODDiskDataBuffer = null;
        fastDiskBufferInitializeToken.set(false);
        memoryBufferInitializeToken.set(false);
        nonCODDiskBufferInitializeToken.set(false);
    }
}
