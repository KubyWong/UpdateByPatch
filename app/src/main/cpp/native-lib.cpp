#include <jni.h>
#include <string>

//声明main函数,与外部bspatch.c文件的main函数关联
extern int main(int i, char **argv);

//写一个动态库的对外接口函数去调用bsdiff_main生成拆分包
extern "C"
JNIEXPORT jstring JNICALL
Java_com_bluetree_myapplication_MainActivity_increaseUpdate(JNIEnv *env, jobject instance,
                                                            jstring oldApk_, jstring patch_,
                                                            jstring newApk_) {
    const char *oldApk = env->GetStringUTFChars(oldApk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *newApk = env->GetStringUTFChars(newApk_, 0);

    const char *returnValue = newApk;
    //*******注意这里的元素顺序******
    char *argv[4] = {""
                     ,const_cast<char *>(oldApk)
                     ,const_cast<char *>(newApk)
                     ,const_cast<char *>(patch)};
    main(4, argv);

    env->ReleaseStringUTFChars(oldApk_, oldApk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(newApk_, newApk);


    return env->NewStringUTF(returnValue);
}