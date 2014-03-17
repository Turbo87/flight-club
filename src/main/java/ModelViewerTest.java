//Unit test the framework
//Run ModelViewerTestApp from the command line to invoke this
//extension of the modelviewer

public class ModelViewerTest extends ModelViewer {
	
	GliderUser glider;
	
	public void init(ModelEnv a)
	{
		super.init(a);
		
		glider = new GliderUser(this, new Vector3d(0,0,1));
		glider.takeOff(new Vector3d(0,0,(float) 0.5));
		cameraMan.subject1 = glider;

		compass = new Compass(this);
		slider = new DataSlider(this);
		slider.label = "V";
	}
	
	public void tick(Clock c)
	{
		super.tick(c);
		compass.setArrow(glider.v.x, glider.v.y);
		slider.setValue(glider.v.z * 200);
		System.out.println(glider.v.z);
		//slider.setValue(1);
	}	
}