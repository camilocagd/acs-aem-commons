/*
 * Copyright 2017 Adobe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.acs.commons.mcp.impl.processes;

import com.adobe.acs.commons.fam.ActionManager;
import com.adobe.acs.commons.fam.ActionManagerFactory;
import com.adobe.acs.commons.fam.impl.ActionManagerFactoryImpl;
import com.adobe.acs.commons.mcp.ControlledProcessManager;
import com.adobe.acs.commons.mcp.ProcessInstance;
import com.adobe.acs.commons.mcp.impl.ProcessInstanceImpl;
import static com.adobe.acs.commons.fam.impl.ActionManagerTest.*;
import com.adobe.acs.commons.mcp.impl.AbstractResourceImpl;
import com.adobe.acs.commons.mcp.util.DeserializeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Tests a few cases for folder relocator
 */
public class FolderRelocatorTest {

    @Test
    public void testInit() throws LoginException, DeserializeException, RepositoryException {
        FolderRelocator tool = new FolderRelocator();
        ProcessInstance instance = new ProcessInstanceImpl(getControlledProcessManager(), tool, "relocator test");
        ResourceResolver rr = getMockResolver();
        AbstractResourceImpl mockFolderA = new AbstractResourceImpl("/content/folderA", "", "", new ResourceMetadata());
        when(rr.getResource("/content/folderA")).thenReturn(mockFolderA);
        AbstractResourceImpl mockFolderB = new AbstractResourceImpl("/content/folderB", "", "", new ResourceMetadata());
        when(rr.getResource("/content/folderB")).thenReturn(mockFolderB);
        AbstractResourceImpl mockFolder = new AbstractResourceImpl("/content", "", "", new ResourceMetadata());
        when(rr.getResource("/content")).thenReturn(mockFolder);
        mockFolder.addChild(mockFolderA);
        mockFolder.addChild(mockFolderB);
        
        Session ses = mock(Session.class);
        when(rr.adaptTo(Session.class)).thenReturn(ses);
        AccessControlManager acm = mock(AccessControlManager.class);
        when(ses.getAccessControlManager()).thenReturn(acm);
        when(acm.privilegeFromName(any())).thenReturn(mock(Privilege.class));
        
        assertEquals("Folder relocator: relocator test", instance.getName());
        try {
            instance.init(getMockResolver(), Collections.EMPTY_MAP);
            fail("That should have thrown an error");
        } catch (DeserializeException ex) {
            // Expected
        }
        Map<String, Object> values = new HashMap<>();
        values.put("sourcePaths", "/content/folderA");
        values.put("destinationPath", "/content/folderB");
        values.put("mode", FolderRelocator.Mode.MOVE.toString());
        instance.init(rr, values);
        instance.run(rr);
    }

    private ControlledProcessManager getControlledProcessManager() throws LoginException {
        ActionManager am = getActionManager();

        ActionManagerFactory amf = mock(ActionManagerFactoryImpl.class);
        when(amf.createTaskManager(any(), any(), anyInt())).thenReturn(am);

        ControlledProcessManager cpm = mock(ControlledProcessManager.class);
        when(cpm.getActionManagerFactory()).thenReturn(amf);
        return cpm;
    }

}
