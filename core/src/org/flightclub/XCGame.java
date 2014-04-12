package org.flightclub;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.Vector;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class XCGame extends ApplicationAdapter {
    public Environment environment;
    public PerspectiveCamera cam;
    public ModelBatch modelBatch;
    public Model model;
    public ModelInstance instance;

    public FirstPersonCameraController camController;

    @Override
	public void create () {
        // Set background color to white
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

        // Create lighting environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 1f, 1f, 1f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Create batch renderer
        modelBatch = new ModelBatch();

        // Create camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.up.set(0, 0, 1);
        cam.position.set(0, -10, 0);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        model = createBoxModel();
        instance = new ModelInstance(model);

        // Initialize camera controller
        camController = new FirstPersonCameraController(cam);
        Gdx.input.setInputProcessor(camController);
	}

    private Model createBoxModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        int attributes = Usage.Position | Usage.Normal;

        modelBuilder.begin();
        MeshBuilder partBuilder = (MeshBuilder) modelBuilder.part("box", GL20.GL_TRIANGLES, attributes, material);

        // chord
        float y = (float) 0.2;
        // nose a bit up
        float z = y * (float) 0.3;
        // anhedral
        float a = (float) 0.15;
        // sweep
        float s = (float) 0.4;

        Vector3 corner00 = new Vector3(0, y, z);
        Vector3 corner01 = new Vector3(1, y - s, z + a);
        Vector3 corner10 = new Vector3(0, 0, 0);
        Vector3 corner11 = new Vector3(1, -s, a);
        Vector3 normal = new Vector3(corner00).lerp(corner01, 0.5f).crs(new Vector3(corner00).lerp(corner10, 0.5f)).nor();
        partBuilder.rect(corner00, corner10, corner11, corner01, normal);
        partBuilder.rect(corner00, corner01, corner11, corner10, normal.scl(-1));

        corner01 = new Vector3(-1, y - s, z + a);
        corner11 = new Vector3(-1, -s, a);
        normal = new Vector3(corner00).lerp(corner01, 0.5f).crs(new Vector3(corner00).lerp(corner10, 0.5f)).nor();
        partBuilder.rect(corner00, corner10, corner11, corner01, normal);
        partBuilder.rect(corner00, corner01, corner11, corner10, normal.scl(-1));

        return modelBuilder.end();
    }

    @Override
    public void render() {
        camController.update();

        instance.transform.rotate(1, 0, 0, Gdx.graphics.getDeltaTime() * 60);
        instance.calculateTransforms();

        // Update viewport
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render the scene
        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }
}
