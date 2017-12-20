package net.ncguy.tracking.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import net.ncguy.tracking.geometry.BaseGeometry;
import net.ncguy.tracking.geometry.GeometryItem;
import net.ncguy.tracking.geometry.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderPreprocessor {

    public static String LoadShader(FileHandle handle) {
        return LoadShader(handle, null);
    }

    public static String LoadShader(FileHandle handle, String outputFile) {
        StringBuilder sb = new StringBuilder();
        try {
            LoadShader_Impl(sb, handle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = sb.toString();

        if(outputFile != null && !outputFile.isEmpty()) {
            try {
                Files.write(new File(outputFile).toPath(), s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s;
    }

    public static void LoadShader_Impl(StringBuilder sb, FileHandle handle) throws IOException {

        String path = handle.parent().path() + "/";

        BufferedReader reader = new BufferedReader(new InputStreamReader(handle.read()));
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if(line.startsWith("#pragma"))
                HandlePragma(sb, path, line);
        }
    }

    public static void HandlePragma(StringBuilder sb, String currentPath, String line) {
        String cmd = line.substring("#pragma ".length());
        if(cmd.startsWith("include")) {
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(line);
            String includePath = null;
            if(m.find())
                includePath = m.group(1);
            if(includePath != null)
                Include(sb, currentPath, includePath);
        }
    }


    public static void Include(StringBuilder sb, String currentPath, String includePath) {
        String path = currentPath + includePath;
        FileHandle handle = Gdx.files.internal(path);
        if(handle.exists()) {
            try {
                LoadShader_Impl(sb, handle);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void CleanGeometryOnShader(ShaderProgram shader, String arrayPrefix, int amt) {
        for (int i = 0; i < amt; i++) {
            String prefix = arrayPrefix + "[" + i + "]";
            shader.setUniformi(prefix + ".valid", 0);
        }
    }

    public static void BindGeometryToShader(ShaderProgram shader, BaseGeometry g, String uniform) {
        shader.setUniformi(uniform + ".id", g.GetId().ordinal());
        shader.setUniform4fv(uniform + ".vec4A", g.GetVec4A().Values(), 0, 4);
        shader.setUniform4fv(uniform + ".vec4B", g.GetVec4B().Values(), 0, 4);
        shader.setUniformf(uniform + ".floatVar", g.GetFloatVar());
        shader.setUniformi(uniform + ".valid", 1);
    }

    public static void BindGeometrySetToShader(ShaderProgram shader, String arrayPrefix, BaseGeometry... geometry) {
        CleanGeometryOnShader(shader, arrayPrefix, 64);
        for (int i = 0; i < geometry.length; i++) {
            BaseGeometry g = geometry[i];
            String prefix = arrayPrefix + "[" + i + "]";
            BindGeometryToShader(shader, g, prefix);
        }
    }

    public static void BindModelToShader(ShaderProgram shader, Model model, String uniform) {
        Matrix4 transform = model.Transform();

        try {
            transform = transform.inv();
        }catch(Exception e) {}

        shader.setUniformMatrix(uniform + ".transform", transform);
        shader.setUniformi(uniform + ".operation", model.operation.ordinal());
        shader.setUniformf(uniform + ".colour", model.colour);
        BindGeometryToShader(shader, model.geometry, uniform);
    }

    public static void BindModelSetToShader(ShaderProgram shader, String uniformPrefix, Model... models) {
        BindModelSetToShader(shader, uniformPrefix, Arrays.asList(models));
    }

    public static void BindModelSetToShader(ShaderProgram shader, String uniformPrefix, List<Model> models) {
        BindModelSetToShader(shader, uniformPrefix, models, false);
    }
    public static void BindModelSetToShader(ShaderProgram shader, String uniformPrefix, List<Model> models, boolean force) {
//        CleanGeometryOnShader(shader, uniformPrefix, 64);
        for (int i = 0; i < models.size(); i++) {
            Model m = models.get(i);
            if(force || m.isDynamic) {
                String prefix = uniformPrefix + "[" + i + "]";
                BindModelToShader(shader, m, prefix);
            }
        }
    }

    public static void BindGLItemSetToShader(ShaderProgram shader, String uniformPrefix, List<GeometryItem.GLItem> items, int maxAmt) {
        for (int i = 0; i < maxAmt; i++) {

            GeometryItem.GLItem item = null;
            if(items.size() > i)
                item = items.get(i);

            String prefix = uniformPrefix+ "[" + i + "]";
            shader.setUniformi(prefix + ".valid", item != null ? 1 : 0);
            if(item != null) {
                shader.setUniformi(prefix + ".leftItem", item.leftItem);
                shader.setUniformi(prefix + ".rightItem", item.rightItem);
                shader.setUniformi(prefix + ".data", item.data);
                shader.setUniformi(prefix + ".operation", item.operation);
            }
        }
    }

}
