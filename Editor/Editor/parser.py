import scene
import javalang
import os

from typing import List
from javalang.tree import *

def get_files(folder_path:str,type:str):
    result = []
    for root, dirs, files in os.walk(folder_path):
        for f in files:
            if f.endswith(type):
                result.append(os.path.join(root, f))
    return result

def parse_component(tree: CompilationUnit) -> scene.Comp:
    comp = scene.Comp("","")

    pkg = tree.package.name if tree.package else None

    for path, node in tree.filter(javalang.tree.ClassDeclaration):

        full_name = f"{pkg}.{node.name}" if pkg else node.name

        comp.name = node.name
        comp.full_name = full_name
        node: ClassDeclaration

        for field in node.fields:
            if 'public' in field.modifiers:

                ftype = field.type.name
                ptype = getattr(field.type, "package", None)

                for decl in field.declarators:
                    init = decl.initializer
                    name = decl.name
                    value = None

                    if init:
                        if isinstance(init, Literal):
                            value = init.value
                        else:
                            value = str(init)
                    if ftype == "GameObject":
                        comp.vars.append(scene.Var(name, ftype, value, special=scene.SPECIAL_VAR_GOBJ))
                    else:
                        comp.vars.append(scene.Var(name, ftype, value))
    return comp

def parse_component_file(path:str) -> List[scene.Comp]:
    loaded_comps: List[scene.Comp] = []
    files = get_files(path,".java")
    for file in files:
        print(file)
        source = open(file).read()
        tree = javalang.parse.parse(source)
        for path, node in tree.filter(javalang.tree.ClassDeclaration):
            if node.extends and node.extends.name == scene.COMPONENT_TYPE:
                loaded_comps.append(parse_component(tree))
                break
    return loaded_comps

