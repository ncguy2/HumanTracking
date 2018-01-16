package net.ncguy.tracking;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.math.*;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.data.TrackedPoint;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.MeshBone;
import net.ncguy.api.tracker.ITracker;
import net.ncguy.skeleton.BoneJoint;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.TrackedBones;
import net.ncguy.utils.ArrayUtils;
import net.ncguy.utils.MathsUtils;
import net.ncguy.utils.collection.QuaternionQueue;
import net.ncguy.utils.tween.QuaternionInterpolator;
import net.ncguy.utils.tween.SKJointTweenAccessor;

import java.util.ArrayList;
import java.util.Collection;

import static net.ncguy.api.data.TrackedPoint.Hint.*;

public class TrackerPipeline extends ArrayList<ITracker> {

    public static float timestep = 0.05f;
    public static boolean threadAlive = true;

    float currentTimestep = 0f;
    TrackedBuffer buffer;
    boolean isActive = true;
    QuaternionQueue headRotationQueue;

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
        headRotationQueue = new QuaternionQueue(5);
        Thread t = new Thread(this::ThreadLoop);
        t.setDaemon(true);
        t.start();
    }

    public boolean ShouldApplyBuffer(float delta) {
        currentTimestep += delta;

        if(currentTimestep > timestep) {
            currentTimestep -= timestep;
            return true;
        }

        return false;
    }

    public void ApplyCurrentBuffer(SKJoint structure) {
        SKJoint headBone = TrackedBones.HeadBone();
        TrackedPoint headPoint = buffer.Get(HEAD);

        if(headPoint != null) {

            if(headPoint.isDirty) {
                if (headPoint.useWorldCoords)
                    ApplyCurrentBufferWorld(structure, headBone, headPoint);
                else ApplyCurrentBuffer(structure, headBone, headPoint);
                headPoint.isDirty = false;
            }
        }

        SKJoint eyeLeftBot = TrackedBones.EyeLeftBotBone();
        SKJoint eyeLeftTop = TrackedBones.EyeLeftTopBone();
        TrackedPoint botLeftEye = buffer.Get(BOT_LEFT_EYE);
        TrackedPoint topLeftEye = buffer.Get(TOP_LEFT_EYE);

        TrackedPoint leftEye = buffer.Get(LEFT_EYE);
        TrackedPoint cornerLeftEye = buffer.Get(LEFT_EYE_CORNER);

        float eyeThresholdClosed = 30f;

        if(ArrayUtils.NotNull(eyeLeftBot, eyeLeftTop, botLeftEye, topLeftEye, leftEye, cornerLeftEye)) {

            float width = leftEye.GetScreenPos().dst(cornerLeftEye.GetScreenPos());
            float dist = botLeftEye.GetScreenPos().dst(topLeftEye.GetScreenPos());

            float perc = dist / width;
            perc *= (2.6086957f);
//            perc -= .2f;
            
            Vector3 target = new Vector3();
            target.set(MathsUtils.Lerp(eyeLeftBot.GetHomePosition(), eyeLeftTop.GetHomePosition(), perc));
            Tween.to(eyeLeftTop, SKJointTweenAccessor.POSITION, timestep).target(target.x, target.y, target.z).start(AppListener.tweenManager);
        }

        SKJoint eyeRightBot = TrackedBones.EyeRightBotBone();
        SKJoint eyeRightTop = TrackedBones.EyeRightTopBone();
        TrackedPoint botRightEye = buffer.Get(BOT_RIGHT_EYE);
        TrackedPoint topRightEye = buffer.Get(TOP_RIGHT_EYE);

        TrackedPoint rightEye = buffer.Get(RIGHT_EYE);
        TrackedPoint cornerRightEye = buffer.Get(RIGHT_EYE_CORNER);

        if(ArrayUtils.NotNull(eyeRightBot, eyeRightTop, botRightEye, topRightEye, rightEye, cornerRightEye)) {

            float width = rightEye.GetScreenPos().dst(cornerRightEye.GetScreenPos());
            float dist = botRightEye.GetScreenPos().dst(topRightEye.GetScreenPos());

            float perc = dist / width;
            perc *= (2.6086957f);
//            perc -= .2f;
            
            Vector3 target = new Vector3();
            target.set(MathsUtils.Lerp(eyeRightBot.GetHomePosition(), eyeRightTop.GetHomePosition(), perc));
            Tween.to(eyeRightTop, SKJointTweenAccessor.POSITION, 50f/1000f).target(target.x, target.y, target.z).start(AppListener.tweenManager);
        }


        CalculateJaw();

        if(true)
            return;

        SKJoint jawBone = TrackedBones.JawBone();
        TrackedPoint jawPoint = buffer.Get(JAW);

        SKJoint mouthTop = TrackedBones.MouthTopBone();

        SKJoint mouthLeft = TrackedBones.JawLeftBone();
        SKJoint mouthRight = TrackedBones.JawRightBone();

        TrackedPoint topPoint = buffer.Get(MOUTH);
        TrackedPoint leftPoint = buffer.Get(MOUTH_LEFT);
        TrackedPoint rightPoint = buffer.Get(MOUTH_RIGHT);

        if(ArrayUtils.NotNull(mouthTop, jawBone, mouthLeft, mouthRight, leftPoint, rightPoint)) {
            Vector3 mouthTopPos = mouthTop.GetPosition();
            Vector3 mouthBotPos = jawBone.GetPosition();

            Vector3 midPoint = MathsUtils.Lerp(mouthTopPos, mouthBotPos, .3f);

            Vector2 lineStart = new Vector2(topPoint.GetScreenPos().x, topPoint.GetScreenPos().y);
            Vector2 lineEnd = new Vector2(jawPoint.GetScreenPos().x, jawPoint.GetScreenPos().y);

            float vertDist = lineStart.dst(lineEnd);

//            float leftDist = MathsUtils.DistanceToLine(leftPoint.GetScreenPos(), lineStart, lineEnd);
//            float rightDist = MathsUtils.DistanceToLine(rightPoint.GetScreenPos(), lineStart, lineEnd);

            float leftDist = leftPoint.GetScreenPos().dst(rightPoint.GetScreenPos()) * .5f;
            float rightDist = leftPoint.GetScreenPos().dst(rightPoint.GetScreenPos()) * .5f;

            float msVertDist = new Vector2(mouthTopPos.x, mouthTopPos.y).dst(mouthBotPos.x, mouthBotPos.y);

            float factor;
            if(vertDist > 1.5)
                factor = msVertDist / vertDist;
            else factor = 0;

            leftDist *= factor;
            rightDist *= factor;

            Vector3 leftDir  = MathsUtils.GetDirection(new Vector3(midPoint.x, midPoint.y, 0), new Vector3(mouthLeft.GetHomePosition().x, mouthLeft.GetHomePosition().y, 0)).scl(leftDist);
            Vector3 rightDir = MathsUtils.GetDirection(new Vector3(midPoint.x, midPoint.y, 0), new Vector3(mouthRight.GetHomePosition().x, mouthRight.GetHomePosition().y, 0)).scl(rightDist);

            if(factor > 0) {
                mouthLeft.SetPosition(leftDir.scl(.01f).add(midPoint));
                mouthRight.SetPosition(rightDir.scl(.01f).add(midPoint));
            }else {
                leftDir.set(mouthLeft.GetHomePosition());
                rightDir.set(mouthRight.GetHomePosition());
            }

            leftDir.z = MathsUtils.Lerp(midPoint.z, mouthLeft.GetHomePosition().z, factor);
            rightDir.z = MathsUtils.Lerp(midPoint.z, mouthRight.GetHomePosition().z, factor);

            mouthLeft.SetPosition(leftDir);
            mouthRight.SetPosition(rightDir);
        }

    }

    void CalculateMapping(SKJoint bone, TrackedPoint.Hint pointHint) {
        TrackedPoint globalPoint = buffer.Get(GLOBAL);
        TrackedPoint point = buffer.Get(pointHint);

        if (!ArrayUtils.NotNull(bone, point, globalPoint))
            return;

        Vector3 screenPoint = new Vector3(point.GetScreenPos(), 1);

        float focalLength = globalPoint.angle;
        float centerX = globalPoint.GetScreenPos().x;
        float centerY = globalPoint.GetScreenPos().y;

        Matrix3 rotMat = MathsUtils.CreateRotationMatrix(globalPoint.worldDir);


        Matrix4 transform = new Matrix4();
        transform.idt();
        transform.rotate(globalPoint.WorldDirQuat());
        transform.translate(globalPoint.worldPos);


//        transform.val[Matrix4.M00] = rotMat.val[Matrix3.M00];
//        transform.val[Matrix4.M10] = rotMat.val[Matrix3.M10];
//        transform.val[Matrix4.M20] = rotMat.val[Matrix3.M20];
//
//        transform.val[Matrix4.M01] = rotMat.val[Matrix3.M01];
//        transform.val[Matrix4.M11] = rotMat.val[Matrix3.M11];
//        transform.val[Matrix4.M21] = rotMat.val[Matrix3.M21];
//
//        transform.val[Matrix4.M02] = rotMat.val[Matrix3.M02];
//        transform.val[Matrix4.M12] = rotMat.val[Matrix3.M12];
//        transform.val[Matrix4.M22] = rotMat.val[Matrix3.M22];


//        transform.val[Matrix4.M30] = globalPoint.worldPos.x;
//        transform.val[Matrix4.M31] = globalPoint.worldPos.y;
//        transform.val[Matrix4.M32] = globalPoint.worldPos.z;

        transform.val[Matrix4.M03] = 0;
        transform.val[Matrix4.M13] = 0;
        transform.val[Matrix4.M23] = 0;
        transform.val[Matrix4.M33] = 1;

//        screenPoint.scl(1.5f);

        screenPoint.mul(transform.inv());

        float scalar = 1.1f;
        SKJoint parent = bone.GetParentJoint();
        screenPoint = parent.InvTransform(screenPoint);

        screenPoint.scl(scalar, -scalar, scalar);
        screenPoint.add(parent.GetHomePosition());
//        screenPoint = parent.Transform(screenPoint);
//        screenPoint.x = bone.GetHomePosition().x;
//        screenPoint.z = bone.GetHomePosition().z;
//        screenPoint = bone.InvTransform(screenPoint);
        bone.SetPosition(screenPoint);


//        Matrix4 screenToWorld = new Matrix4(globalPoint.worldPos, globalPoint.WorldDirQuat(), new Vector3(1, 1, 1));
//        Vector3 jawScreenPos = new Vector3(point.GetScreenPos(), 1);
//        jawScreenPos.mul(screenToWorld);
//
//        jawScreenPos = bone.InvTransform(jawScreenPos);
//
//        Vector3 homePos = bone.GetHomePosition();
//        jawScreenPos.scl(.1f).add(homePos);
//        bone.SetPosition(jawScreenPos);
    }


    void CalculateJaw() {
//        CalculateMapping(TrackedBones.JawBone(), JAW);
//        CalculateMapping(TrackedBones.EyeLeftTopBone(), TOP_LEFT_EYE);
//        CalculateMapping(TrackedBones.EyeLeftBotBone(), BOT_LEFT_EYE);
//
//        CalculateMapping(TrackedBones.EyeRightTopBone(), TOP_RIGHT_EYE);
//        CalculateMapping(TrackedBones.EyeRightBotBone(), BOT_RIGHT_EYE);

//        if(jawBone != null && jawPoint != null) {
//            if(jawPoint.isDirty) {
//
//                Vector3 sub = new Vector3(0, (jawPoint.angle - 1) * .15f, 0);
//                Vector3 pos = jawBone.GetHomePosition().cpy().sub(sub);
//
//                pos = jawBone.InvTransform(pos);
//
//                MutableFloat mutableFloat = new MutableFloat(jawBone.InvTransform(jawBone.GetPosition()).y) {
//                    @Override
//                    public void setValues(MutableFloat target, int tweenType, float[] newValues) {
//                        super.setValues(target, tweenType, newValues);
//                        Vector3 cpy = jawBone.InvTransform(jawBone.GetPosition().cpy());
//                        cpy.y = target.floatValue();
//
//                        float x = cpy.x;
//                        float z = cpy.z;
//
////                        cpy.y -= .01f;
//                        cpy = jawBone.Transform(cpy);
//                        cpy.x = x;
//                        cpy.z = z;
//
//                        jawBone.SetPosition(cpy);
//                    }
//                };
//                Tween.to(mutableFloat, 1, timestep).target(pos.y).start(AppListener.tweenManager);
//
////                jawBone.SetPosition(pos);
//
//                jawPoint.isDirty = false;
//            }
//        }
    }

    public void ApplyCurrentBuffer(SKJoint structure, SKJoint joint, TrackedPoint point) {
        if(point != null) {
            joint.SetPosition(new Vector3(point.GetScreenPos(), 0));

            TrackedPoint lEyePoint = buffer.Get(LEFT_EYE);
            TrackedPoint rEyePoint = buffer.Get(RIGHT_EYE);

            if (lEyePoint != null && rEyePoint != null) {
                Vector2 l = lEyePoint.GetScreenPos();
                Vector2 r = rEyePoint.GetScreenPos();
                double deg = Math.toDegrees(Math.atan((l.y - r.y) / (l.x - r.x)));

                if (joint instanceof BoneJoint) {
                    Bone bone = ((BoneJoint) joint).bone;
                    if (bone instanceof MeshBone) {
                        MeshBone meshBone = (MeshBone) bone;
                        meshBone.node.rotation.setEulerAngles(0, 0, (float) deg);
                    }
                }
            }else {
                if (joint instanceof BoneJoint) {
                    Bone bone = ((BoneJoint) joint).bone;
                    if (bone instanceof MeshBone) {
                        MeshBone meshBone = (MeshBone) bone;
                        meshBone.node.rotation.setEulerAngles(0, 0, (float) point.angle);
                    }
                }
            }
        }
    }

    public void ApplyCurrentBufferWorld(SKJoint structure, SKJoint joint, TrackedPoint point) {
        if(point != null) {
//            joint.SetPosition(new Vector3(point.GetScreenPos(), 0));
            if (joint instanceof BoneJoint) {
                Bone bone = ((BoneJoint) joint).bone;
                if (bone instanceof MeshBone) {
                    MeshBone meshBone = (MeshBone) bone;
//                    meshBone.node.rotation.setEulerAngles(0, 0, (float) point.angle);

                    Vector3 d = point.worldDir;
//                    Quaternion quaternion = MathsUtils.ToQuaternion(new Vector3(d.x, d.y, d.z));
//                    meshBone.node.rotation.set(quaternion);

                    Quaternion q = new Quaternion().setEulerAnglesRad(-d.y, d.x, -d.z);

                    headRotationQueue.Push(q);

//                    joint.SetDirection(new Vector3(-d.y, d.x, -d.z));


                    Quaternion quaternion = headRotationQueue.GetAverage();
//                    meshBone.node.rotation.set(quaternion);
//                    meshBone.Update();
//                    meshBone.node.calculateTransforms(true);
//                    meshBone.node.calculateBoneTransforms(true);

//                    joint.SetPosition(joint.GetPosition());

                    QuaternionInterpolator qi = new QuaternionInterpolator(meshBone.node.rotation, quaternion, () -> meshBone.node.rotation, meshBone::Update);
                    Tween.to(qi, 1, timestep).target(1f).start(AppListener.tweenManager);

                }
            }
//            joint.SetDirection(point.worldDir);
        }
    }


    protected void ThreadLoop() {
        while(threadAlive) {
            if(isActive) {
                Track();
            }else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void Track() {
        forEach(this::Track);
    }

    protected void Track(ITracker t) {
        t.Track(buffer);
    }

}
