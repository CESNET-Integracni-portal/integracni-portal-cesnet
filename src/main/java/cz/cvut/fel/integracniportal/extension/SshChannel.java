/*
 * Copyright 2014 Simon So
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
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import cz.cvut.fel.integracniportal.exceptions.ServiceAccessException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SshChannel {

    private static final Logger logger = Logger.getLogger(SshChannel.class);

    @Autowired
    private SshDataSource sshDataSource;

    private InputStream in;
    private BufferedReader inBuffered;

    private ChannelExec sshChannel;

    @PostConstruct
    private void init() throws Exception {

        sshChannel = sshDataSource.getSshChannel();
        if (sshChannel == null) {
            logger.error("Cannot obtain an ssh channel.");
            throw new Exception("Cannot obtain an ssh channel.");
        }

        in = sshChannel.getInputStream();
        inBuffered = new BufferedReader(new InputStreamReader(in));

    }

    public List<String> sendCommand(String command) throws ServiceAccessException {
        try {
            sshChannel.setCommand(command);
            sshChannel.connect();
            List<String> response = readResponse();
            sshChannel.disconnect();

            return response;
        } catch (IOException e) {
            logger.error("Unable to read response.");
            throw new ServiceAccessException("cesnet.service.badResponse");
        } catch (JSchException e) {
            logger.error("Unable to connect to Cesnet.");
            throw new ServiceAccessException("cesnet.service.unavailable");
        }
    }

    private List<String> readResponse() throws IOException {
        List<String> response = new ArrayList<String>();
        String line;
        while ((line = inBuffered.readLine()) != null) {
            response.add(line);
        }
        return response;
    }

    @PreDestroy
    private void destroy() {
        if (sshChannel != null) {
            logger.debug("destroying the pooled resource...");
            returnToPool();
            sshChannel.disconnect();
        }
    }

    public void returnToPool() {
        try {
            sshDataSource.returnSession(sshChannel.getSession());
        } catch (Exception e) {
            // invalid session?
        }
    }

    public SshDataSource getSshDataSource() {
        return sshDataSource;
    }

    public void setSshDataSource(SshDataSource sshDataSource) {
        this.sshDataSource = sshDataSource;
    }

}
