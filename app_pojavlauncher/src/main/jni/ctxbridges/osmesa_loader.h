//
// Created by Vera-Firefly on 28.08.2023.
//

#ifndef POJAVLAUNCHER_OSMESA_LOADER_H
#define POJAVLAUNCHER_OSMESA_LOADER_H

#include <GL/osmesa.h>

extern GLboolean (*OSMesaMakeCurrent_p) (OSMesaContext ctx, void *buffer, GLenum type,
                                         GLsizei width, GLsizei height);
extern OSMesaContext (*OSMesaGetCurrentContext_p) (void);
extern OSMesaContext  (*OSMesaCreateContext_p) (GLenum format, OSMesaContext sharelist);
extern void (*OSMesaDestroyContext_p) (OSMesaContext ctx);
extern void (*OSMesaPixelStore_p) ( GLint pname, GLint value );
extern GLubyte* (*glGetString_p) (GLenum name);
extern void (*glFinish_p) (void);
extern void (*glClearColor_p) (GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
extern void (*glClear_p) (GLbitfield mask);
extern void (*glReadPixels_p) (GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, void * data);
void dlsym_OSMesa();
void dlsym_OSMesa_1();
#endif //POJAVLAUNCHER_OSMESA_LOADER_H
