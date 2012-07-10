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
package gov.nasa.arc.mct.buffer.disk.internal;

import java.util.Properties;

import gov.nasa.arc.mct.buffer.config.DataBufferEnv;
import gov.nasa.arc.mct.buffer.config.DiskBufferEnv;
import gov.nasa.arc.mct.buffer.internal.DataBufferHelper;
import gov.nasa.arc.mct.buffer.internal.MetaDataBuffer;
import gov.nasa.arc.mct.buffer.internal.PartitionDataBuffer;

public class NonCODDiskDataBufferHelper implements DataBufferHelper {
    public final PartitionDataBuffer newPartitionBuffer(int partitionNo) {
        return new PartitionNonCODDiskBuffer(partitionNo);
    }
    
    public final PartitionDataBuffer newPartitionBuffer(DataBufferEnv env) {
        assert env instanceof DiskBufferEnv;
        return new PartitionNonCODDiskBuffer((DiskBufferEnv)env);
    }
    
    @Override
    public MetaDataBuffer newMetaDataBuffer(DataBufferEnv env) {
        if (env == null) {
            return new MetaDiskBuffer(new DiskBufferEnv(null));
        }
        assert env instanceof DiskBufferEnv;
        return new MetaDiskBuffer((DiskBufferEnv)env);
    }
    
    @Override
    public DataBufferEnv newMetaDataBufferEnv(Properties prop) {
        return new DiskBufferEnv(prop);
    }
}
