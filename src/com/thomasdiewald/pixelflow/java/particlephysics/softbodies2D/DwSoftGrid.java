package com.thomasdiewald.pixelflow.java.particlephysics.softbodies2D;

import java.util.Random;

import com.thomasdiewald.pixelflow.java.particlephysics.DwParticle2D;
import com.thomasdiewald.pixelflow.java.particlephysics.DwPhysics;
import com.thomasdiewald.pixelflow.java.particlephysics.DwSpringConstraint;
import com.thomasdiewald.pixelflow.java.particlephysics.DwSpringConstraint2D;

public class DwSoftGrid extends DwSoftBody2D{
  

  // specific attributes for this body
  public int   nodes_x;
  public int   nodes_y;
  public float nodes_r;
  
  public int bend_spring_mode = 0;
  public int bend_spring_dist = 3; // try other values, it affects the objects stiffness
  
  Random rand;
  
  public DwSoftGrid(){
  }

  public void create(DwPhysics<DwParticle2D> physics, int nx, int ny, float nr, float start_x, float start_y){
 
    this.physics            = physics;
    this.rand               = new Random(0);
    this.collision_group_id = physics.getNewCollisionGroupId();
    this.nodes_offset       = physics.getParticlesCount();
    this.nodes_x            = nx;
    this.nodes_y            = ny;
    this.nodes_r            = nr;
    this.num_nodes          = nodes_x * nodes_y;
    this.particles          = new DwParticle2D[num_nodes];
    
    DwParticle2D.MAX_RAD = Math.max(DwParticle2D.MAX_RAD, nr);

    // temp variables
    int idx, idx_world;
    int x, y, ox, oy;
    float px, py;
    
    // 1) init particles
    for(y = 0; y < nodes_y; y++){
      for(x = 0; x < nodes_x; x++){
        idx            = y * nodes_x + x;
        idx_world      = idx + nodes_offset;
        px             = start_x + x * nodes_r * 2;
        py             = start_y + y * nodes_r * 2;
        particles[idx] = new CustomParticle2D(idx_world, px, py, nodes_r);
        particles[idx].setParamByRef(param_particle);
        particles[idx].setRadiusCollision(nodes_r * collision_radius_scale);
        particles[idx].collision_group = collision_group_id;
        if(self_collisions){
          particles[idx].collision_group = physics.getNewCollisionGroupId();
        }
      }
    }
    
    
    ox = bend_spring_dist;
    oy = bend_spring_dist;
 
    // 2) create springs
    for(y = 0; y < nodes_y; y++){
      for(x = 0; x < nodes_x; x++){
        
        if(CREATE_STRUCT_SPRINGS){
          addSpring(x, y, -1, 0, DwSpringConstraint.TYPE.STRUCT);
          addSpring(x, y,  0,-1, DwSpringConstraint.TYPE.STRUCT);
          addSpring(x, y, +1, 0, DwSpringConstraint.TYPE.STRUCT);
          addSpring(x, y,  0,+1, DwSpringConstraint.TYPE.STRUCT);
        }
                  
        if(CREATE_SHEAR_SPRINGS){
          addSpring(x, y, -1,-1, DwSpringConstraint.TYPE.SHEAR);
          addSpring(x, y, +1,-1, DwSpringConstraint.TYPE.SHEAR);
          addSpring(x, y, -1,+1, DwSpringConstraint.TYPE.SHEAR);
          addSpring(x, y, +1,+1, DwSpringConstraint.TYPE.SHEAR);
        }
        
        if(CREATE_BEND_SPRINGS && bend_spring_dist > 0){
          // diagonal
          if(bend_spring_mode == 0){
            addSpring(x, y, -ox, -oy, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y, +ox, -oy, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y, -ox, +oy, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y, +ox, +oy, DwSpringConstraint.TYPE.BEND);
          }
          
          // orthogonal
          if(bend_spring_mode == 1){
            addSpring(x, y, -ox,   0, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y, +ox,   0, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y,   0, +oy, DwSpringConstraint.TYPE.BEND);
            addSpring(x, y,   0, -oy, DwSpringConstraint.TYPE.BEND);
          }
          
          // random, 'kind of' anisotropic
          if(bend_spring_mode == 2){
            for(int i = 0; i < 8; i++){
              ox = (int) Math.round((rand.nextFloat()*2-1) * bend_spring_dist);
              oy = (int) Math.round((rand.nextFloat()*2-1) * bend_spring_dist);
              
//              float ra = (float)(rand.nextFloat() * Math.PI * 2);
//              float rx = (float)(Math.cos(ra));
//              float ry = (float)(Math.sin(ra));
//              
//              float rand_rad = 1.5f + bend_spring_dist * rand.nextFloat();
//              ox = (int) Math.round(rx * rand_rad);
//              oy = (int) Math.round(ry * rand_rad);
              addSpring(x, y, ox, oy, DwSpringConstraint.TYPE.BEND);
            }
          }
        }
        
      }
    }
    
    
    // add new particles to the physics-world
    physics.addParticles(particles, num_nodes);
  }
  
 
  public DwParticle2D getNode(int x, int y){
    if(x < nodes_x && y < nodes_y){
      int idx = y *nodes_x + x;
      return particles[idx];
    } else {
      return null;
    }
  }
  
  
  public void addSpring(int ax, int ay, int offx, int offy, DwSpringConstraint.TYPE type){
    int bx = ax + offx;
    int by = ay + offy;
    
    // clamp offset to grid-bounds
    if(bx < 0) bx = 0; else if(bx > nodes_x-1) bx = nodes_x-1;
    if(by < 0) by = 0; else if(by > nodes_y-1) by = nodes_y-1;

    int ia = ay * nodes_x + ax;
    int ib = by * nodes_x + bx;

    DwSpringConstraint2D.addSpring(physics, particles[ia], particles[ib], param_spring, type);
  }
  

}
  
  
  
 
  