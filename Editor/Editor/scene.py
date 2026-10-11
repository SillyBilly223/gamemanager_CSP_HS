from typing import List

from PySide6.QtWidgets import QTreeWidgetItem
from PySide6.QtCore import QRect, QRectF, QPoint
from PySide6.QtCore import Qt

COMPONENT_TYPE = 'Component'

SPECIAL_VAR_DEFAULT = ""
SPECIAL_VAR_GOBJ = "gobj"

NAV_PAR = "pt"
NAV_CHLD = "cd"

GET_GOBJ = "gj"
GET_COMP = "cm"

class Var:
    def __init__(self, name:str, vartype:str, value:str, special:str=None):
        self.name = name
        self.vartype = vartype
        self.value = value
        self.special = SPECIAL_VAR_DEFAULT if special is None else special
        pass
    def clone(self):
        return Var(self.name, self.vartype, self.value, self.special)
    def to_dict(self):
        return {
            "name": self.name,
            "vartype": self.vartype,
            "value": self.value,
            "special": self.special
        }
    
    @staticmethod
    def from_dict(d):
        return Var(d["name"],d["vartype"],d["value"],d["special"])
class Vector3:
    def __init__(self,x:float=0,y:float=0,z:float=0):
        self.x = x
        self.y = y
        self.z = z
        pass
    def clone(self):
        return Vector3(self.x,self.y,self.z)
    def to_dict(self):
        return { "x": self.x, "y": self.y, "z": self.z }
    
    @staticmethod
    def from_dict(d):
        return Vector3(d["x"],d["y"],d["z"])
class Vector2:
    def __init__(self,x:float=0,y:float=0):
        self.x = x
        self.y = y
        pass
    def clone(self):
        return Vector2(self.x,self.y)
    def to_dict(self):
        return { "x": self.x, "y": self.y }
    
    @staticmethod
    def from_dict(d):
        return Vector2(d["x"],d["y"])
class Transform:
    def __init__(self, pos:Vector3=None, scale:Vector2=None):
        self.position:Vector3 = Vector3() if pos is None else pos
        self.scale:Vector2 = Vector2(1,1) if scale is None else scale
        pass
    def clone(self):
        return Transform(self.position.clone(),self.scale.clone())
    def to_dict(self):
        return { "position": self.position.to_dict(), "scale": self.scale.to_dict() }
        
    def to_center_rect(self):
        return QRectF(
            self.position.x - self.scale.x / 2,
            self.position.y - self.scale.y / 2,
            self.scale.x,
            self.scale.y
        )
    
    def to_rect(self):
        return Vector2(self.position.x + (self.scale.x/2), self.position.y + (self.scale.y/2))

    @staticmethod
    def from_dict(d):
        pos = Vector3.from_dict(d["position"])
        scale = Vector2.from_dict(d["scale"])
        return Transform(pos, scale)
class Comp:
    def __init__(self,name,full_name):
        self.name:str = name
        self.full_name:str = full_name
        self.vars: List[Var] = []
        pass
    def clone(self):
        comp = Comp(self.name,self.full_name)
        for var in self.vars:
            comp.vars.append(var.clone())
        return comp
    def to_dict(self):
        return { "name": self.name, "full_name": self.full_name, "vars": [v.to_dict() for v in self.vars] }
    
    @staticmethod
    def from_dict(d):
        comp = Comp(d["name"],d["full_name"])
        for v in d["vars"]:
            comp.vars.append(Var.from_dict(v))
        return comp
class Sprite:
    def __init__(self,name:str="none"):
        self.name:str = name
    def clone(self):
        return Sprite(self.name)
    def to_dict(self):
        return { "name": self.name }
    
    @staticmethod
    def from_dict(d):
        return Sprite(d["name"])
class GameObject:
    def __init__(self,name, parent=None):
        self.name:str = name
        self.enabled = True

        self.sprite = Sprite()
        self.transform = Transform()

        self.parent:GameObject = parent
        self.children:List[GameObject] = []

        self.comps: List[Comp] = []
    def clone(self,parent=None):
        gobj = GameObject(self.name,parent) 
        gobj.enabled = self.enabled

        gobj.sprite = self.sprite.clone()
        gobj.transform = self.transform.clone()

        for comp in self.comps:
            gobj.comps.append(comp.clone())
        for child in self.children:
            gobj.children.append(child.clone(gobj))

        return gobj
    def contains_comp(self,name:str):
        return any(comp.name == name for comp in self.comps)
    
    def to_list(self):
        out = [(self, self.name)]
        for child in self.children:
            out.extend(child.to_list())
        return out
    def to_dict(self):
        return {
            "name": self.name,
            "enabled": self.enabled,
            "sprite": self.sprite.to_dict(),
            "transform": self.transform.to_dict(),
            "comps": [c.to_dict() for c in self.comps],
            "children": [child.to_dict() for child in self.children]
        }
    
    @staticmethod
    def from_dict(d, parent=None):
        gobj = GameObject(d["name"],parent)
        gobj.enabled = d["enabled"]
        gobj.sprite = Sprite.from_dict(d["sprite"])
        gobj.transform = Transform.from_dict(d["transform"])
        for comp in d["comps"]:
            gobj.comps.append(Comp.from_dict(comp))
        for child in d["children"]:
            gobj.children.append(GameObject.from_dict(child, gobj))
        return gobj       
class Prefab:
    def __init__(self,gobj:GameObject):
        self.gobj = gobj
    def clone(self, parent:GameObject = None):
        return Prefab(self.gobj.clone(parent))
    def to_dict(self):
        return {
            "gobj": self.gobj.to_dict()
        }
    
    @staticmethod
    def from_dict(d):
        if "gobj" in d:
            return Prefab(GameObject.from_dict(d["gobj"]))
        else:
            return Prefab(GameObject.from_dict(d))
class Scene:
    def __init__(self, name:str, gobj:GameObject):
        self.name = name
        self.root = gobj
    def to_dict(self):
        return {
            "name": self.name,
            "root": self.root.to_dict()
        }
    
    @staticmethod
    def from_dict(d):
        return Prefab(d["name"], GameObject.from_dict(d["root"]))

def gobj_to_tree(gobj:GameObject) -> QTreeWidgetItem:
    
    tree = QTreeWidgetItem([gobj.name])
    tree.setData(0, Qt.UserRole, gobj)
    children = gobj.children

    for child in children:
        if len(child.children) > 0:
            sub_tree = gobj_to_tree(child) 
            tree.addChild(sub_tree)
        else:
            sub_gobj = QTreeWidgetItem([child.name])
            sub_gobj.setData(0, Qt.UserRole, child)
            tree.addChild(sub_gobj)         

    return tree

'''
    ---navigators
    pt = get parent - depth (depth navigates through all parents)
    cd = get child - index

    --getter
    gj = return gobj - none
    cm = return component - index
'''

def get_parent_depth(gobj: GameObject) -> int:
    depth = 0
    while gobj:
        gobj = gobj.parent
        if gobj:
            depth += 1
    return depth

def parents_to_list(gobj: GameObject) -> List[GameObject]:
    parents = []
    while gobj:
        parents.append(gobj)
        gobj = gobj.parent
    return parents

def get_intersecting_parent(gobj_main: GameObject, gobj_search: GameObject):
    main_ancestors = set(parents_to_list(gobj_main))
    depth = 0
    cur = gobj_search
    while cur:
        if cur in main_ancestors:
            return cur, depth
        cur = cur.parent
        depth += 1
    return None, -1

def is_child_of(gobj_main: GameObject, gobj_child: GameObject):
    stack = [(c, 1) for c in gobj_main.children]
    while stack:
        node, depth = stack.pop()
        if node is gobj_child:
            return True, depth
        for c in node.children:
            stack.append((c, depth + 1))
    return False, -1

def get_nav_to_child(root: GameObject, targ: GameObject) -> str:
    stack = [(root, [])]  # DFS
    while stack:
        node, path = stack.pop()
        if node is targ:
            parts = [f"{NAV_CHLD}|{i}" for i in path]
            parts.append(GET_GOBJ)
            return ":".join(parts)
        for i, c in enumerate(node.children):
            stack.append((c, path + [i]))
    return ""

def get_nav_to_gobj(src: GameObject, targ: GameObject) -> str:
    intersect, up_depth = get_intersecting_parent(targ, src)
    if intersect is None:
        return ""
    parts = []
    if up_depth > 0:
        parts.append(f"{NAV_PAR}|{up_depth}")
    down = get_nav_to_child(intersect, targ)
    if down:
        parts.append(down)
    return ":".join(parts)

def get_nav_to_comp(gobj: GameObject, targ: Comp) -> str:
    if targ not in gobj.comps:
        return f"{GET_COMP}|0"
    idx = gobj.comps.index(targ)
    return f"{GET_GOBJ}:{GET_COMP}|{idx}"

def navigate_to_gobj(root: GameObject, path: str) -> GameObject | None:
    steps = path.split(":")
    current = root

    for step in steps:
        if step.startswith(NAV_PAR + "|"):
            try:
                depth = int(step.split("|")[1])
            except ValueError:
                return None
            for _ in range(depth):
                if current.parent is None:
                    return None
                current = current.parent

        elif step.startswith(NAV_CHLD + "|"):
            try:
                idx = int(step.split("|")[1])
            except ValueError:
                return None
            if idx < 0 or idx >= len(current.children):
                return None
            current = current.children[idx]

        elif step == GET_GOBJ:
            return current
        else:
            return None
        
    return current



