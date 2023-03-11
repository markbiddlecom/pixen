package com.brotherhoodgames.pixen.mod.tree;

import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.ToString;

/** Represents an individual "chord" in a {@link GiantRedwoodGenerator}'s trunk. */
@Builder(builderClassName = "Builder")
@ToString
/*package*/ final class TrunkChord {
  private double x;
  private double z;
  private double rx;
  private double rz;

  private double a;
  private double va;
  private double ad;

  private double vx;
  private double vz;
  private double g;

  /** Indicates that the chord should update its settings as the trunk grows one level in height. */
  void climb() {
    // Update angle
    a += va;
    va *= ad;

    // Update position
    x += vx;
    z += vz;

    // Apply gravity
    double theta = Math.atan2(-z, -x);
    double gx = g * Math.cos(theta);
    double gz = g * Math.sin(theta);
    vx += gx;
    vz += gz;
  }

  double distance(double tx, double tz) {
    double dx = tx - x;
    double dz = tz - z;
    double theta = Math.atan2(dz, dx) - a;
    double rxSinTheta = rx * Math.sin(theta);
    double rzCosTheta = rz * Math.cos(theta);
    double rTheta = Math.sqrt(rxSinTheta * rxSinTheta + rzCosTheta * rzCosTheta);
    double d = Math.sqrt(dx * dx + dz * dz);
    return Math.max(0, d - rTheta);
  }

  static final class Builder {
    /**
     * Indicates that the chord's center point should lie at the normalized origin.
     *
     * @return {@code this} instance for method chaining.
     */
    @Nonnull
    Builder atOrigin() {
      this.x = 0;
      this.z = 0;
      return this;
    }

    @Nonnull
    Builder size(double r, double eccentricity) {
      this.rx = r;
      this.rz = rx * Math.sqrt(1 - Math.pow(eccentricity, 2));
      return this;
    }

    @Nonnull
    Builder unmoving() {
      this.ad = 1;
      this.vx = this.vz = this.va = 0;
      this.g = 0;
      return this;
    }
  }
}
