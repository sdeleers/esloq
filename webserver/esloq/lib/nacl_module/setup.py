from distutils.core import setup, Extension

module = Extension("nacl", 
        extra_objects=["nacl/build/slock2/lib/amd64/libnacl.a"],
        sources=["naclmodule.c"])

setup(name= "naclPackage",
        version = "1.0",
        description = "Nacl package",
        ext_modules = [module])
