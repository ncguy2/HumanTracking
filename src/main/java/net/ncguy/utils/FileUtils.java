package net.ncguy.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import net.ncguy.utils.tools.FBXConv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileUtils {

    static AssetManager assets = new AssetManager();

    public static Model LoadModel(String filePath) {

        String path = filePath + ".g3dj";

        if(assets.isLoaded(path))
            return assets.get(path, Model.class);

        if(filePath.toLowerCase().endsWith(".fbx") || filePath.toLowerCase().endsWith(".obj")) {
            File file = new File(path);
            if(!Files.exists(file.toPath()))
                new FBXConv().InvokeSafe(System.out::println, "-f", "-v", filePath, path);
        }


        assets.load(path, Model.class);
        assets.finishLoadingAsset(path);

//        FileHandle handle = Gdx.files.absolute(new File(path).getAbsolutePath());
//        JsonReader reader = new JsonReader();
//        G3dModelLoader loader = new G3dModelLoader(reader);
//        ModelData modelData = loader.parseModel(handle);

        Model model = assets.get(path, Model.class);

        model.materials.forEach(mtl -> {
            if(mtl.has(BlendingAttribute.Type)) {
                BlendingAttribute attr = (BlendingAttribute) mtl.get(BlendingAttribute.Type);
                attr.opacity = 1.f;
            }
        });

        return model;
    }

    public static boolean WriteByteBuffer(ByteBuffer buffer, OutputStream stream) {
        try {
            WriteByteBuffer_Impl(buffer, stream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Optional<ByteBuffer> ReadByteBuffer(InputStream stream) {
        try {
            return Optional.of(ReadByteBuffer_Impl(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public static void WriteByteBuffer_Impl(ByteBuffer buffer, OutputStream stream) throws IOException {
        buffer.position(0);
        stream.write(buffer.array());
        buffer.position(0);
    }

    public static ByteBuffer ReadByteBuffer_Impl(InputStream is) throws IOException {
        List<Byte> bytes = new ArrayList<>();

//        while((c = (byte) is.read()) != -1)
//            bytes.add(c);

        while(is.available() > 0)
            bytes.add((byte) is.read());


        byte[] buffer = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            buffer[i] = bytes.get(i);

        return ByteBuffer.wrap(buffer);
    }

}
