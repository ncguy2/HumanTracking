package net.ncguy.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
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
import java.util.function.Consumer;

public class FileUtils {

    public static Model LoadModel(String filePath) {
        return LoadModel(filePath, true);
    }

    public static Model LoadModel(String filePath, boolean appendToPath) {

        filePath = filePath.replace("\\", "/");

        String path = filePath;
        if(appendToPath)
            path += ".g3dj";

        AssetUtils assets = AssetUtils.instance();

        if(assets.IsLoaded(path))
            return assets.Get(path, Model.class);

        if(filePath.toLowerCase().endsWith(".fbx") || filePath.toLowerCase().endsWith(".obj")) {
            File file = new File(path);
            if(!Files.exists(file.toPath())) {
                new FBXConv().InvokeSafeLong("FBX Conversion: ", "Converting \"" + filePath + "\" to usable format", null, "-f", "-v", filePath, path);
            }
        }

        Model model = assets.Get(path, Model.class);

        model.materials.forEach(mtl -> {
            if(mtl.has(BlendingAttribute.Type)) {
                BlendingAttribute attr = (BlendingAttribute) mtl.get(BlendingAttribute.Type);
                attr.opacity = 1.f;
            }else mtl.set(new BlendingAttribute(1.f));
        });

        return model;
    }

    public static void LoadModelAsync(String filePath, Consumer<Model> func) {
        LoadModelAsync(filePath, true, func);
    }

    public static void LoadModelAsync(String filePath, boolean appendToPath, Consumer<Model> func) {
        filePath = filePath.replace("\\", "/");

        String path = filePath;
        if(appendToPath)
            path += ".g3dj";

        AssetUtils assets = AssetUtils.instance();

        if(assets.IsLoaded(path)) {
            func.accept(assets.Get(path, Model.class));
            return;
        }

        if(filePath.toLowerCase().endsWith(".fbx") || filePath.toLowerCase().endsWith(".obj")) {
            File file = new File(path);
            if(!Files.exists(file.toPath())) {
                new FBXConv().InvokeSafeLong("FBX Conversion: ", "Converting \"" + filePath + "\" to usable format", null, "-f", "-v", filePath, path);
            }
        }

        assets.GetAsync(path, Model.class, model -> {
            model.materials.forEach(mtl -> {
                if(mtl.has(TextureAttribute.Diffuse)) {
                    TextureAttribute attr = (TextureAttribute) mtl.get(TextureAttribute.Diffuse);
                    System.out.println(attr);
                    attr.textureDescription.magFilter = Texture.TextureFilter.Nearest;
                    attr.textureDescription.minFilter = Texture.TextureFilter.Nearest;
                }

                if(mtl.has(TextureAttribute.Emissive)) {
                    TextureAttribute attr = (TextureAttribute) mtl.get(TextureAttribute.Emissive);
                    System.out.println(attr);
                    attr.textureDescription.magFilter = Texture.TextureFilter.Nearest;
                    attr.textureDescription.minFilter = Texture.TextureFilter.Nearest;
                }

                if(mtl.has(BlendingAttribute.Type)) {
                    BlendingAttribute attr = (BlendingAttribute) mtl.get(BlendingAttribute.Type);
                    attr.opacity = .99f;

//                    attr.sourceFunction = GL20.GL_SRC_ALPHA;
//                    attr.destFunction = GL20.GL_ONE_MINUS_SRC_ALPHA;
//                    attr.opacity = .99f;
//                    attr.blended = true;
                    mtl.set(new FloatAttribute(FloatAttribute.AlphaTest, .1f));
                }

//                mtl.set(new DepthTestAttribute(GL20.GL_LESS));
            });

            func.accept(model);
        });

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
