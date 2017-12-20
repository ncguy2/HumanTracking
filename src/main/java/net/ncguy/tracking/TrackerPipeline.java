package net.ncguy.tracking;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.data.TrackedPoint;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.MeshBone;
import net.ncguy.api.tracker.ITracker;
import net.ncguy.skeleton.BoneJoint;
import net.ncguy.skeleton.SKJoint;

import java.util.ArrayList;
import java.util.Collection;

public class TrackerPipeline extends ArrayList<ITracker> {

    TrackedBuffer buffer;
    boolean isActive = true;

    public TrackerPipeline() {
        buffer = new TrackedBuffer();
    }

    public TrackerPipeline(int initialCapacity) {
        super(initialCapacity);
        buffer = new TrackedBuffer();
    }

    public TrackerPipeline(Collection<? extends ITracker> c) {
        super(c);
        buffer = new TrackedBuffer();
    }

    public void StartTracker() {
        Thread t = new Thread(this::ThreadLoop);
        t.setDaemon(true);
        t.start();
    }

    public void ApplyCurrentBuffer(SKJoint structure) {
        SKJoint headBone = structure.Find("Head");
        TrackedPoint headPoint = buffer.Get(TrackedPoint.Hint.HEAD);

        if(headPoint != null) {
            headBone.SetPosition(new Vector3(headPoint.GetScreenPos(), 0));

            TrackedPoint lEyePoint = buffer.Get(TrackedPoint.Hint.LEFT_EYE);
            TrackedPoint rEyePoint = buffer.Get(TrackedPoint.Hint.RIGHT_EYE);

            if (lEyePoint != null && rEyePoint != null) {
                Vector2 l = lEyePoint.GetScreenPos();
                Vector2 r = rEyePoint.GetScreenPos();
                double deg = Math.toDegrees(Math.atan((l.y - r.y) / (l.x - r.x)));

                if (headBone instanceof BoneJoint) {
                    Bone bone = ((BoneJoint) headBone).bone;
                    if (bone instanceof MeshBone) {
                        MeshBone meshBone = (MeshBone) bone;
                        meshBone.node.rotation.setEulerAngles(0, 0, (float) deg);
                    }
                }
            }else {
                if (headBone instanceof BoneJoint) {
                    Bone bone = ((BoneJoint) headBone).bone;
                    if (bone instanceof MeshBone) {
                        MeshBone meshBone = (MeshBone) bone;
                        meshBone.node.rotation.setEulerAngles(0, 0, (float) headPoint.angle);
                    }
                }
            }
        }
    }

    protected void ThreadLoop() {
        while(true) {
            if(isActive) {
                Track();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void Track() {
        buffer.clear();
        forEach(this::Track);
    }

    protected void Track(ITracker t) {
        t.Track(buffer);
    }

}
