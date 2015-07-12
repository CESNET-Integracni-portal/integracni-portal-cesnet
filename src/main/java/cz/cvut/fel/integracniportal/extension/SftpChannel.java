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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SftpChannel {

    private static final Logger logger = Logger.getLogger(SftpChannel.class);

    @Autowired
    private SshDataSource sshDataSource;

    private ChannelSftp sftpChannel;


    @PostConstruct
    private void init() throws Exception {

        sftpChannel = sshDataSource.getSftpChannel();
        if (sftpChannel == null) {
            throw new Exception("Problem obtaining an ssh channel.");
        }

        sftpChannel.connect();
    }

    public void cd(String path) throws SftpException {
        sftpChannel.cd(path);
    }

    public InputStream getFile(String filename) throws SftpException, IOException {
        InputStream inputStream = sftpChannel.get(filename);
        ByteArrayOutputStream clonedStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, clonedStream);
        clonedStream.flush();
        InputStream result = new ByteArrayInputStream(clonedStream.toByteArray());
        sftpChannel.disconnect();
        return result;
    }

    public void mkdir(String path) throws SftpException {
        sftpChannel.mkdir(path);
    }

    public void renameFolder(String oldPath, String newPath) throws SftpException {
        sftpChannel.rename(oldPath, newPath);
    }

    public void deleteFile(String filename) throws SftpException {
        sftpChannel.rm(filename);
    }

    public void deleteFolder(String path) throws SftpException {
        sftpChannel.rmdir(path);
    }

    public void uploadFile(InputStream fileStream, String filename) throws SftpException {
        sftpChannel.put(fileStream, filename);
    }

    public void renameFile(String oldPath, String newPath) throws SftpException {
        sftpChannel.rename(oldPath, newPath);
    }

    public void moveFile(String oldPath, String newPath) throws SftpException {
        sftpChannel.rename(oldPath, newPath);
    }

    @PreDestroy
    private void destroy() {
        if (sftpChannel != null) {
            logger.debug("destroying the pooled resource...");
            returnToPool();
        }
    }

    boolean isValid() {
        return !sftpChannel.isClosed();
    }

    public void returnToPool() {
        try {
            sshDataSource.returnSession(sftpChannel.getSession());
        } catch (Exception e) {
            // invalid session?
        }
        sftpChannel.disconnect();
    }

    public SshDataSource getSshDataSource() {
        return sshDataSource;
    }

    public void setSshDataSource(SshDataSource sshDataSource) {
        this.sshDataSource = sshDataSource;
    }

}
