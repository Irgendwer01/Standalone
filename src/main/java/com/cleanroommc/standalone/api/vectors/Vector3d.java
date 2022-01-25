package com.cleanroommc.standalone.api.vectors;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class Vector3d {

    public double x;
    public double y;
    public double z;

    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d(@Nonnull Vector3d other) {
        this(other.x, other.y, other.z);
    }

    public Vector3d(@Nonnull BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(@Nonnull Vector3d vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public Vector3d add(@Nonnull Vector3d vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public void sub(@Nonnull Vector3d vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
    }

    @Nonnull
    public Vector3d negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public void scale(double s) {
        x *= s;
        y *= s;
        z *= s;
    }

    public void scale(double sx, double sy, double sz) {
        x *= sx;
        y *= sy;
        z *= sz;
    }

    public void normalize() {
        double scale = 1.0 / Math.sqrt(x * x + y * y + z * z);
        scale(scale);
    }

    public double dot(@Nonnull Vector3d other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public void cross(@Nonnull Vector3d v1, @Nonnull Vector3d v2) {
        x = v1.y * v2.z - v1.z * v2.y;
        y = v2.x * v1.z - v2.z * v1.x;
        z = v1.x * v2.y - v1.y * v2.x;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distanceSquared(@Nonnull Vector3d v) {
        double dx, dy, dz;
        dx = x - v.x;
        dy = y - v.y;
        dz = z - v.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    public double distance(Vector3d v) {
        return Math.sqrt(distanceSquared(v));
    }

    @Override
    public String toString() {
        return "Vector3d(" + x + ", " + y + ", " + z + ")";
    }

    public void abs() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
    }

    @Nonnull
    public Vec3d getVec3() {
        return new Vec3d(x, y, z);
    }
}