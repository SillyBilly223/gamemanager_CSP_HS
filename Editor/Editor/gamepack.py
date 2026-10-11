import json
import base64
import scene
import os

from PySide6.QtGui import QPixmap
from typing import List, Dict

class Sprite:
    def __init__(self, name:str, data:bytes):
        self.name = name
        self.data = data
        self.pixmap = QPixmap()
        self.pixmap.loadFromData(self.data)
        pass

    def to_dict(self):
        return {
            "name": self.name,
            "data": base64.b64encode(self.data).decode("ascii")
        }

    @staticmethod
    def from_dict(d):
        data = base64.b64decode(d["data"].encode("ascii"))
        return Sprite(d["name"], data)

class GamePacket: 
    def __init__(self):
        self.scenes:Dict[str,scene.Scene] = {}
        self.prefabs:Dict[str,scene.Prefab] = {}
        self.sprites:Dict[str,Sprite] = {}

    def clear(self):
        self.scenes.clear()
        self.prefabs.clear()
        self.sprites.clear()

    def scene_save(self, scene:scene.Scene):
        if self.name in self.scenes: 
            return False
        self.scenes[self.name] = self
    def secen_remove(self, name):
        if name in self.scenes: del self.scenes[name]

    def prefab_save(self, prefab:scene.Prefab):
        if prefab.gobj.name in self.prefabs: 
            return False
        self.prefabs[prefab.gobj.name] = prefab
    def prefab_remove(self, name):
        if name in self.prefabs: del self.prefabs[name]

    def sprite_save(self, path):
        with open(path, "rb") as f:
            name = os.path.basename(path)
            if name in self.sprites:
                return False
            data = f.read()

        self.sprites[name] = Sprite(name, data)
        return True
    def sprite_remove(self, name):
        if name in self.sprites: del self.sprites[name]
  
    def save_packet(self, path: str):
        with open(path, "w") as f:
            json.dump(self.to_dict(), f, indent=4)

    def to_dict(self):
        print(f"Gamepack To Dict Scene:{len(self.scenes)} Prefabs:{len(self.prefabs)} Sprites:{len(self.sprites)}")
        return {
            "scenes": [scene.to_dict() for scene in self.scenes.values()],
            "prefabs": [prefab.to_dict() for prefab in self.prefabs.values()],
            "sprites": [sprite.to_dict() for sprite in self.sprites.values()]
        }
    
    @staticmethod
    def from_dict(d: dict):
        packet = GamePacket()
        
        scenes = d.get("scenes")
        if scenes:
            for p_scene in scenes:
                d_scene = scene.Scene.from_dict(p_scene)
                packet.scenes[d_scene.name] = d_scene

        prefabs = d.get("prefabs")
        if prefabs:
            for p_prefab in prefabs:
                d_prefab = scene.Prefab.from_dict(p_prefab)
                packet.prefabs[d_prefab.gobj.name] = d_prefab

        sprites = d.get("sprites")
        if sprites:
            for p_sprite in sprites:
                d_sprite = Sprite.from_dict(p_sprite)
                packet.sprites[d_sprite.name] = d_sprite

        return packet



