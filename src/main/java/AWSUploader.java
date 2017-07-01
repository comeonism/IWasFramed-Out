import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AWSUploader {
    public boolean uploadImage(String dirName, File image) throws IOException {
        String keyName = dirName + "/" + image.getName();

        String md5 = calculateMD5(image);
        AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
        AmazonS3 s3client = new AmazonS3Client(credentials);

        PutObjectResult result = s3client.putObject(new PutObjectRequest(Utils.BUCKET_NAME, keyName, image));

        // check if successfully uploaded
        return md5.equals(result.getETag());
    }

    private String calculateMD5(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        fis.close();

        return md5;
    }
}
