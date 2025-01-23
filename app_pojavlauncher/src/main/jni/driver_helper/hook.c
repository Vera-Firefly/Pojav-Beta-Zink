//
// Created by maks on 05.06.2023.
// Modifiled by Vera-Firefly on 17.01.2025.
//

#include <android/dlext.h>
#include <string.h>
#include <stdio.h>

static void* (*android_dlopen_ext_impl)(const char* filename, int flags, const android_dlextinfo* extinfo, const void* caller_addr);
static struct android_namespace_t* (*android_get_exported_namespace_impl)(const char* name);

static void* ready_handle;

static const char* supported_namespaces[] = {"sphal", "vendor", "default"};

__attribute__((visibility("default"), used))
void linker_hook_set_handles(void* handle, void* dlopen_ext, void* get_namespace) {
    ready_handle = handle;
    android_dlopen_ext_impl = dlopen_ext;
    android_get_exported_namespace_impl = get_namespace;
}

__attribute__((visibility("default"), used))
void* android_dlopen_ext(const char* filename, int flags, const android_dlextinfo* extinfo) {
    if (strstr(filename, "vulkan."))
        return ready_handle;

    return android_dlopen_ext_impl(filename, flags, extinfo, &android_dlopen_ext);
}

__attribute__((visibility("default"), used))
void* android_load_sphal_library(const char* filename, int flags) {
    if (strstr(filename, "vulkan."))
        return ready_handle;

    struct android_namespace_t* androidNamespace;
    for (int i = 0; i < 3; i++)
    {
        androidNamespace = android_get_exported_namespace_impl(supported_namespaces[i]);

        if (androidNamespace != NULL)
            break;
    }

    android_dlextinfo extinfo = {
        .flags = ANDROID_DLEXT_USE_NAMESPACE,
        .library_namespace = androidNamespace
    };

    return android_dlopen_ext_impl(filename, flags, &extinfo, &android_dlopen_ext);
}

__attribute__((visibility("default"), used))
uint64_t atrace_get_enabled_tags() {
    return 0;
}