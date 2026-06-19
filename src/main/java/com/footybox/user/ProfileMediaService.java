package com.footybox.user;

import com.footybox.common.NotFoundException;
import com.footybox.security.CurrentUserService;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileMediaService {
    private static final long MAX_UPLOAD = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 24_000_000L;
    private final CurrentUserService currentUserService;
    private final AppUserRepository users;
    private final UserProfileMediaRepository media;

    public ProfileMediaService(CurrentUserService currentUserService, AppUserRepository users, UserProfileMediaRepository media) {
        this.currentUserService = currentUserService;
        this.users = users;
        this.media = media;
    }

    @Transactional
    public UserProfileResponse save(MultipartFile file, boolean avatar) {
        AppUser user = currentUserService.currentUser();
        byte[] processed = process(file, avatar ? 512 : 1600, avatar ? 512 : 500);
        UserProfileMedia row = media.findById(user.getId()).orElseGet(UserProfileMedia::new);
        row.setUser(user);
        if (avatar) row.setAvatarData(processed); else row.setBannerData(processed);
        media.save(row);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse delete(boolean avatar) {
        AppUser user = currentUserService.currentUser();
        media.findById(user.getId()).ifPresent(row -> {
            if (avatar) row.setAvatarData(null); else row.setBannerData(null);
            if (row.getAvatarData() == null && row.getBannerData() == null) media.delete(row); else media.save(row);
        });
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public byte[] read(Long userId, boolean avatar) {
        users.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
        return media.findById(userId).map(row -> avatar ? row.getAvatarData() : row.getBannerData()).orElse(null);
    }

    private byte[] process(MultipartFile file, int targetWidth, int targetHeight) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_UPLOAD) throw new IllegalArgumentException("Choose a JPEG or PNG image smaller than 5 MB.");
        String type = file.getContentType();
        if (!("image/jpeg".equals(type) || "image/png".equals(type))) throw new IllegalArgumentException("Only JPEG and PNG images are supported.");
        try {
            byte[] bytes = file.getBytes();
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IllegalArgumentException("The uploaded file is not a valid image.");
                ImageReader reader = readers.next();
                reader.setInput(input);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if ((long) width * height > MAX_PIXELS) throw new IllegalArgumentException("The image dimensions are too large.");
            }
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) throw new IllegalArgumentException("The uploaded file is not a valid image.");
            double targetRatio = (double) targetWidth / targetHeight;
            int cropWidth = source.getWidth(), cropHeight = source.getHeight();
            if ((double) cropWidth / cropHeight > targetRatio) cropWidth = (int) Math.round(cropHeight * targetRatio);
            else cropHeight = (int) Math.round(cropWidth / targetRatio);
            int x = (source.getWidth() - cropWidth) / 2, y = (source.getHeight() - cropHeight) / 2;
            BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = output.createGraphics();
            graphics.setColor(new Color(20, 24, 29)); graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, x, y, x + cropWidth, y + cropHeight, null);
            graphics.dispose();
            return writeJpeg(output);
        } catch (IOException exception) {
            throw new IllegalArgumentException("The uploaded image could not be processed.");
        }
    }

    private byte[] writeJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); params.setCompressionQuality(0.84f);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally { writer.dispose(); }
        return bytes.toByteArray();
    }
}

