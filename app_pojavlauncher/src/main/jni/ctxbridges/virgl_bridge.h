//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef VIRGL_BRIDGE_H
#define VIRGL_BRIDGE_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "osmesa_loader.h"

struct virgl_render_window_t {
    struct ANativeWindow *nativeSurface;
    ANativeWindow_Buffer buffer;
    OSMesaContext context;
};

bool loadSymbolsVirGL();
int virglInit();
void* virglCreateContext(void* contextSrc);
void* virglGetCurrentContext();
void virglMakeCurrent(void* window);
void virglSwapBuffers();
void virglSwapInterval(int interval);

#endif //VIRGL_BRIDGE_H
