package net.ncguy.tracking.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.tracking.render.utils.Object3;

/**
 * Created by Nick on 30/03/2015 at 22:27.
 * Project: Development
 * Package: net.ncguy.commonutil.particle
 */
public class ParticleFactory {

    public static class ParticleInfo {
        public String id = "Particle";
        public String colHex = "1ac6ffff";
        public String posX = "0.0", posY = "0.0", posZ = "0.0";
        public String moveType = "STATIC";
        public String moveSpeed = "1.0";
        public String radius = "25.0";
        public String alphaTimeLow = "0.0", alphaTimeHigh = "1.0";
        public String alphaScaleLow = "1.0", alphaScaleHigh = "0.0";
        public String alphaRangeLow = "1.0", alphaRangeHigh = "1.0";
        public String scaleTimeLow = "0.0", scaleTimeHigh = "1.0";
        public String scaleScaleLow = "0.0", scaleScaleHigh = "1.0";
        public String scaleRangeLow = "0.0", scaleRangeHigh = "1.0";
        public String brownianTimeLow = "0.0", brownianTimeHigh = "100";
        public String brownianScaleLow = "0.0", brownianScaleHigh = "100.0";
        public String brownianRangeLow = "0.0", brownianRangeHigh = "5.0";
        public String emissionLow = "1.0", emissionHigh = "50.0";
        public String lifeLow = "5000.0", lifeHigh = "7500.0";
        public String duration = "1.0";
        public String maxCount = "500";
        public String moveXAxis = "false", moveYAxis = "false", moveZAxis = "false";
        public String angleContX = "SINE", angleContY = "SINE", angleContZ = "SINE";


        public ParticleTranslated translate() {
            ParticleTranslated trans = new ParticleTranslated();
            trans.id = id;
            trans.particleColour = Color.valueOf(colHex);
            trans.position = new Vector3(Float.valueOf(posX), Float.valueOf(posY), Float.valueOf(posZ));
            trans.movementType = ParticleMovement.valueOf(moveType.toUpperCase());
            trans.moveSpeed = Float.valueOf(moveSpeed);
            trans.radius = Float.valueOf(radius);
            trans.alphaTimeline = new Vector2();
            trans.alphaScale = new Vector2();
            trans.alphaRange = new Vector2();
            trans.scaleTimeline = new Vector2();
            trans.scaleScale = new Vector2();
            trans.scaleRange = new Vector2();
            trans.brownianTimeline = new Vector2();
            trans.brownianScale = new Vector2();
            trans.brownianRange = new Vector2();
            trans.emissionRange = new Vector2();
            trans.lifeRange = new Vector2();

            trans.alphaTimeline.x = Float.valueOf(alphaTimeLow);
            trans.alphaTimeline.y = Float.valueOf(alphaTimeHigh);
            trans.alphaScale.x = Float.valueOf(alphaScaleLow);
            trans.alphaScale.y = Float.valueOf(alphaScaleHigh);
            trans.alphaRange.x = Float.valueOf(alphaRangeLow);
            trans.alphaRange.y = Float.valueOf(alphaRangeHigh);
            trans.scaleTimeline.x = Float.valueOf(scaleTimeLow);
            trans.scaleTimeline.y = Float.valueOf(scaleTimeHigh);
            trans.scaleScale.x = Float.valueOf(scaleScaleLow);
            trans.scaleScale.y = Float.valueOf(scaleScaleHigh);
            trans.scaleRange.x = Float.valueOf(scaleRangeLow);
            trans.scaleRange.y = Float.valueOf(scaleRangeHigh);
            trans.brownianTimeline.x = Float.valueOf(brownianTimeLow);
            trans.brownianTimeline.y = Float.valueOf(brownianTimeHigh);
            trans.brownianScale.x = Float.valueOf(brownianScaleLow);
            trans.brownianScale.y = Float.valueOf(brownianScaleHigh);
            trans.brownianRange.x = Float.valueOf(brownianRangeLow);
            trans.brownianRange.y = Float.valueOf(brownianRangeHigh);
            trans.emissionRange.x = Float.valueOf(emissionLow);
            trans.emissionRange.y = Float.valueOf(emissionHigh);
            trans.lifeRange.x = Float.valueOf(lifeLow);
            trans.lifeRange.y = Float.valueOf(lifeHigh);
            trans.duration = Float.valueOf(duration);
            trans.maxCount = Integer.valueOf(maxCount);
            trans.moveAxis = new Object3<>();
            trans.moveAxis.x = Boolean.valueOf(moveXAxis);
            trans.moveAxis.y = Boolean.valueOf(moveYAxis);
            trans.moveAxis.z = Boolean.valueOf(moveZAxis);
            trans.angleControllers = new Object3<>();
            trans.angleControllers.x = new ParticleEmitter.AngleController(angleContX);
            trans.angleControllers.y = new ParticleEmitter.AngleController(angleContY);
            trans.angleControllers.z = new ParticleEmitter.AngleController(angleContZ);
//            for(int i = 0; i < ParticleMovement.values().length; i++) {
//                if(moveType.toUpperCase().equals(ParticleMovement.values()[i]))
//            }
            return trans;
        }
    }
    public static class ParticleTranslated {
        public ParticleInfo info;

        public String id;
        public Color particleColour;
        public Vector3 position;
        public ParticleMovement movementType;
        public Object3<ParticleEmitter.AngleController> angleControllers;
        public float moveSpeed;
        public float radius;
        public Vector2 alphaTimeline, alphaScale, alphaRange;
        public Vector2 scaleTimeline, scaleScale, scaleRange;
        public Vector2 brownianTimeline, brownianScale, brownianRange;
        public Vector2 emissionRange, lifeRange;
        public float duration;
        public int maxCount;
        public Object3<Boolean> moveAxis;
    }

    public static enum ParticleMovement {
        STATIC,
        RADIAL,
        SPLINE,
        ;
    }

}
