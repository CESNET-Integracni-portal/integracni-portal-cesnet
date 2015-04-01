package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.SftpException;
import cz.cvut.fel.integracniportal.api.*;
import cz.cvut.fel.integracniportal.exceptions.FileAccessException;
import cz.cvut.fel.integracniportal.exceptions.FileNotFoundException;
import cz.cvut.fel.integracniportal.exceptions.ServiceAccessException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Radek Jezdik
 */
@Component
public class CesnetFileRepository implements FileRepository, OfflinableFileRepository, BinFileRepository {

    private static final Logger logger = Logger.getLogger(CesnetFileRepository.class);

    private static final String ROOT_DIR = "VO_storage-cache_tape";

    private static final String HOME_FOLDER_PREFIX = "home_";

    private static final String BIN_FOLDER_PREFIX = "bin_";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @Autowired
    private Provider<SshChannel> sshResourceProvider;

    @Autowired
    private Provider<SftpChannel> sftpChannelChannelProvider;

    @Override
    public String getName() {
        return "CESNET";
    }

    @Override
    public void createFolder(FolderDefinition folder) {
        createFolderPath(getUserHomeFolder(folder.getOwner()), folder.getPath().split("/", -1));
    }

    private void createFolderPath(String rootFolder, String[] folderPath) {
        SftpChannel sftpChannel = sftpChannelChannelProvider.get();
        String currentFolder = rootFolder;

        int i = 0;
        do {
            try {
                sftpChannel.cd(currentFolder);
            } catch (SftpException e1) {
                logger.debug("Folder " + currentFolder + " does not exist, trying to create it");
                try {
                    sftpChannel.mkdir(currentFolder);
                    sftpChannel.cd(currentFolder);
                } catch (SftpException e2) {
                    logger.debug("Could not create folder " + currentFolder, e2);
                    throw new ServiceAccessException("Could not create folder", e2);
                }
            }
            if (i == folderPath.length) {
                break;
            }
            currentFolder = folderPath[i++];
        } while (true);
    }

    @Override
    public void moveFolder(FolderDefinition from, FolderDefinition to) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            sftpChannel.renameFolder(getHomeFolderPath(from), getBinFolderPath(to));
        } catch (SftpException e) {
            throw new ServiceAccessException("Could not move folder", e);
        }
    }

    @Override
    public void moveFolderToBin(FolderDefinition folder) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            createFolderPath(getUserBinFolder(folder.getOwner()), folder.getPath().split("/", -1));
            sftpChannel.renameFolder(getHomeFolderPath(folder), getBinFolderPath(folder));
        } catch (SftpException e) {
            throw new ServiceAccessException("Could not move folder to bin", e);
        }
    }

    @Override
    public void renameFolder(String newName, FolderDefinition folder) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            sftpChannel.deleteFolder(getHomeFolderPath(folder));
        } catch (SftpException e) {
            throw new ServiceAccessException("Could not rename folder", e);
        }
    }

    @Override
    public void putFile(FileDefinition file, InputStream stream) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            sftpChannel.cd(getHomeFolderPath(file.getFolder()));
            sftpChannel.uploadFile(stream, file.getName());
        } catch (SftpException e) {
            throw new ServiceAccessException("Could not upload file", e);
        }
    }

    @Override
    public InputStream getFile(FileDefinition file) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            sftpChannel.cd(getHomeFolderPath(file.getFolder()));
            return sftpChannel.getFile(file.getId());
        } catch (Exception e) {
            throw new FileAccessException("Could not get file", e);
        }
    }

    @Override
    public void moveFileToBin(FileDefinition file) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            createFolderPath(getUserBinFolder(file.getOwner()), new String[] { });
            sftpChannel.renameFile(getHomeFolderPath(file.getFolder()) + "/" + file.getId(), getBinFolderPath(file.getFolder()) + "/" + file.getId());
        } catch (SftpException e) {
            throw new ServiceAccessException("Could not move file to bin", e);
        }
    }

    @Override
    public void moveFile(FileDefinition file, FolderDefinition to) {
        try {
            SftpChannel sftpChannel = sftpChannelChannelProvider.get();
            sftpChannel.moveFile(getHomeFolderPath(file.getFolder()) + "/" + file.getId(), getHomeFolderPath(to) + "/" + file.getId());
        } catch (Exception e) {
            throw new ServiceAccessException("Could not move file", e);
        }
    }

    @Override
    public void renameFile(String newName, FileDefinition file) {
        // no code - file names are IDs
    }

    @Override
    public FileDefinition getFileMetadata(FileDefinition file) {
        SshChannel sshChannel = sshResourceProvider.get();

        String filePath = getHomeFolderPath(file.getFolder()) + "/" + file.getId();
        List<String> lsOutput = sshChannel.sendCommand("dmls -l " + filePath);
        if (lsOutput.size() != 1) {
            throw new FileNotFoundException("Could not get file metadata from CESNET");
        }

        return parseFileMetadata(lsOutput.get(0));
    }

    private FileDefinition parseFileMetadata(String lsOutput) {
        String[] parts = lsOutput.split("\\s+");
        if (parts.length < 9) {
            throw new FileAccessException("cesnet.parseError");
        }

        FileDefinition fileDefinition = new FileDefinition();
        fileDefinition.setName(parts[8]);
        fileDefinition.setSize(Long.parseLong(parts[4]));
//        fileMetadata.setState(FileState.valueOf(parts[7].substring(1, parts[7].length() - 1)));
        try {
            fileDefinition.setDateCreated(toDate(parts[5] + " " + parts[6]));
        } catch (ParseException e) {
            logger.error("Unable to parse date " + parts[5] + " and time " + parts[6]);
        }

        return fileDefinition;
    }

    @Override
    public void moveFileOffline(FileDefinition file) {
        SshChannel sshChannel = sshResourceProvider.get();

        String filePath = getHomeFolderPath(file.getFolder()) + "/" + file.getId();
        List<String> response = sshChannel.sendCommand("dmput -r " + filePath);
        if (response.size() > 0) {
            throw new FileAccessException(response.get(0));
        }
    }

    @Override
    public void moveFileOnline(FileDefinition file) {
        SshChannel sshChannel = sshResourceProvider.get();

        String filePath = getHomeFolderPath(file.getFolder()) + "/" + file.getId();
        List<String> response = sshChannel.sendCommand("dmget " + filePath);
        if (response.size() > 0) {
            throw new FileAccessException(response.get(0));
        }
    }

    private String getUserHomeFolder(User user) {
        return ROOT_DIR + "/" + HOME_FOLDER_PREFIX + user.getId();
    }

    private String getUserBinFolder(User user) {
        return ROOT_DIR + "/"  + BIN_FOLDER_PREFIX + user.getId();
    }

    private String getHomeFolderPath(FolderDefinition folder) {
        return getUserHomeFolder(folder.getOwner()) + "/" + folder.getPath();
    }

    private String getBinFolderPath(FolderDefinition folder) {
        return getUserBinFolder(folder.getOwner()) + "/" + folder.getPath();
    }

    public Date toDate(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.parse(date);
    }

    @Override
    public String getType() {
        return "cesnet";
    }
}
