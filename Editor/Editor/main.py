from PySide6.QtWidgets import *
from PySide6.QtGui import QDropEvent, QPixmap, QIcon, QPainter, QColor, QFont, QPen
from PySide6.QtCore import Qt, QPoint, QSize, QTimer, QLine, QRectF

import scene
import gamepack
import parser
import json
import os

from typing import Dict
from typing import List
from tkinter import filedialog

class EditorData():
    def __init__(self):
        
        script_dir = os.path.dirname(os.path.abspath(__file__))
        self.path = os.path.join(script_dir, "editor_data.json")
        print(self.path)
        self.components_directories:List[str] = []
        self.components_lpath:str = ""

        if os.path.exists(self.path):
            with open(self.path, "r") as f:
                data:Dict = json.load(f)
                self.components_directories = data.get("components_directories", [])
                self.components_lpath = data.get("components_lpath", "")
        else:
            self.save()
    def save(self):
            with open(self.path, "w") as f:
                json.dump({
                    "components_directories": self.components_directories,
                    "components_lpath": self.components_lpath
                }, f, indent=4)
    
    def comp_directorie_add(self,path):
        self.components_directories.append(path)
        self.save()
    def comp_directorie_contains(self,path) -> bool:
        return path in self.components_directories
    def comp_directorie_remove(self,index):
        del self.components_directories[index]
        self.save()

global_packet = gamepack.GamePacket()
global_edata = EditorData()
global_loaded_comps:List[scene.Comp] = []
global_loaded_comps_name:List[str] = []

def global_get_comp_clone(name:str):
    index = global_loaded_comps_name.index(name)
    return global_loaded_comps[index].clone()
    pass

class ComponentView(QWidget):

    def __init__(self, parent:QWidget):
        super().__init__(parent)
        
        self.layout_main = QVBoxLayout(self)
        self.layout_dir = QVBoxLayout()
        self.layout_comp = QGridLayout()
    
        self.layout_main.addLayout(self.layout_dir)
        self.layout_main.addLayout(self.layout_comp)

        dir_label = QLabel(text="Component Directories")

        dir_button = QPushButton()
        dir_button.setText("Add Directorie")
        dir_button.clicked.connect(lambda: self.load_directorie_dialog())

        self.dir_list = QListWidget()
        self.dir_list.setObjectName("list_dir")
        self.dir_list.itemDoubleClicked.connect(
            lambda: self.remove_directorie(self.dir_list.currentRow())
            )

        self.layout_dir.addWidget(dir_label)
        self.layout_dir.addWidget(dir_button)
        self.layout_dir.addWidget(self.dir_list)

        wcomp_comp_label = QLabel(text="Components")
        
        self.wcomp_comp_list = QListWidget() 
        self.wcomp_comp_list.setObjectName("comp_lists")
        self.wcomp_comp_list.itemClicked.connect(lambda: self.view_component(self.wcomp_comp_list.currentRow()))

        self.wcomp_view_label = QLabel(text="None")

        self.wcomp_view_list = QListWidget() 
        self.wcomp_view_list.setObjectName("wid_comp_list_view")
        
        self.layout_comp.addWidget(wcomp_comp_label,0,2)
        self.layout_comp.addWidget(self.wcomp_comp_list,1,2)
        self.layout_comp.addWidget(self.wcomp_view_label,0,3)
        self.layout_comp.addWidget(self.wcomp_view_list,1,3)   

        self.reload_view()

    def reload_view(self):
        global global_edata
        global global_loaded_comps
        global global_loaded_comps_name

        self.dir_list.clear()
        self.dir_list.addItems(global_edata.components_directories)

        global_loaded_comps.clear()
        global_loaded_comps_name.clear()

        self.wcomp_comp_list.clear()
        for path in global_edata.components_directories:
            self.load_directorie_path(path)
        
        self.view_component(self.wcomp_comp_list.currentRow())

    def update(self):
        global global_edata

        self.dir_list.clear()
        self.dir_list.addItems(global_edata.components_directories)

        self.wcomp_comp_list.clear()
        self.wcomp_comp_list.addItems([c.name for c in global_loaded_comps])
        self.view_component(self.wcomp_comp_list.currentRow())
    
    def load_directorie_dialog(self):
        global global_edata
        global global_loaded_comps
        global global_loaded_comps_name

        path = QFileDialog(directory=global_edata.components_lpath).getExistingDirectory()
        if path == "": return

        if global_edata.comp_directorie_contains(path): return
        global_edata.components_lpath = path
        global_edata.comp_directorie_add(path)

        comps = parser.parse_component_file(path)
        for comp in comps:
            if comp.name not in global_loaded_comps_name:
                global_loaded_comps.append(comp)
                global_loaded_comps_name.append(comp.name)

        self.wcomp_comp_list.addItems([c.name for c in global_loaded_comps])

        global_edata.save()
        self.update()

    def load_directorie_path(self, path:str):
        global global_edata
        global global_loaded_comps
        global global_loaded_comps_name

        comps = parser.parse_component_file(path)
        for comp in comps:
            if comp.name not in global_loaded_comps_name:
                global_loaded_comps.append(comp)
                global_loaded_comps_name.append(comp.name)

        self.wcomp_comp_list.addItems([c.name for c in global_loaded_comps])

    def remove_directorie(self, index:int):
        global global_edata
        
        global_edata.comp_directorie_remove(index)

        self.reload_view()

    def view_component(self, index:int):
        global global_loaded_comps

        self.wcomp_view_list.clear()

        if index >= len(global_loaded_comps) or index < 0:
            self.wcomp_view_label.setText(f"None")
            return
        
        comp = global_loaded_comps[index]

        self.wcomp_view_label.setText(f"{comp.name}")
        self.wcomp_view_list.addItems([f"{v.name} : {v.vartype} = {v.value}" for v in comp.vars])

class PrefabView(QWidget):
    def __init__(self, parent:QWidget):
        super().__init__(parent)

        self.layout_main = QVBoxLayout(self)
        self.layout_options = QVBoxLayout(self)
        self.layout_inspector = QHBoxLayout(self)

        self.gobj_inspector = GobjInspector()
        self.gobj_tree = GobjTree(self, self.gobj_inspector)
        self.gobj_canvas = GobjCanvas()

        self.current_prefab:scene.Prefab = scene.Prefab(scene.GameObject("GameObject"))

        splitter = QSplitter(Qt.Horizontal)
        splitter.addWidget(self.gobj_tree)
        splitter.addWidget(self.gobj_canvas)
        splitter.addWidget(self.gobj_inspector)
        splitter.setSizes([160, 600, 280])
        
        self.layout_main.addWidget(splitter)
        self.gobj_tree.set_gobj(self.current_prefab.gobj)
        self.gobj_canvas.set_gobj(self.current_prefab.gobj)
    
    def reload_view(self):
        global global_packet

        prefab_list = list(global_packet.prefabs.values())
        if prefab_list:
            self.current_prefab = prefab_list[0]
            self.gobj_tree.set_gobj(self.current_prefab.gobj)
            self.gobj_canvas.set_gobj(self.current_prefab.gobj)
            return

        if self.current_prefab:
            self.gobj_tree.set_gobj(self.current_prefab.gobj)
            self.gobj_canvas.set_gobj(self.current_prefab.gobj)

    def new_prefab(self):
        self.current_prefab = scene.Prefab(scene.GameObject("GameObject"))
        self.gobj_tree.set_gobj(self.current_prefab.gobj)
        self.gobj_canvas.set_gobj(self.current_prefab.gobj)

    def save_prefab(self):
        global global_packet

        if self.current_prefab.gobj.name in global_packet.prefabs.keys():
            reply = QMessageBox.question(None,"Confirm",f"Overwrite {self.current_prefab.gobj.name}?",QMessageBox.Yes | QMessageBox.No)
            if reply == QMessageBox.No: return

        global_packet.prefabs[self.current_prefab.gobj.name] = self.current_prefab.gobj

    def load_prefab(self):
        global global_packet

        prefab_name, ok = QInputDialog.getItem(self, "Load", "Select Prefab:", global_packet.prefabs.keys(), 0, False)
        if not ok or not prefab_name:
            return

        self.current_prefab.gobj = global_packet.prefabs[prefab_name].clone()
        self.gobj_tree.set_gobj(self.current_prefab.gobj)
        self.gobj_canvas.set_gobj(self.current_prefab.gobj)

    def delete_prefab(self):
        global global_packet

        prefab_name, ok = QInputDialog.getItem(self, "Delete", "Select Prefab:", global_packet.prefabs.keys(), 0, False)
        if not ok or not prefab_name:
            return

        del global_packet.prefabs[prefab_name]

class GobjInspector(QTreeWidget):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setColumnCount(2)
        self.setHeaderLabels(["Property", "Value"])
        self.setEditTriggers(QTreeWidget.DoubleClicked | QTreeWidget.SelectedClicked)

    def set_gameobject(self, root:scene.GameObject, gobj:scene.GameObject, item:QTreeWidgetItem):
        self.clear()

        try:
            self.itemChanged.disconnect()
            self.customContextMenuRequested.disconnect()
            self.itemDoubleClicked.disconnect()
        except TypeError:
            pass
        
        self.root_gobj = root
        self.current_gobj = gobj

        self._add_edit_field(
            "Name", 
            gobj.name, setter = lambda v, g=gobj, it=item: (setattr(g, "name", v), 
            it.setText(0, v)), 
            expected_type=str, 
            getter = lambda g=gobj: g.name
        )
        self._add_edit_field(
            "Enabled", 
            gobj.enabled, setter = lambda v, g=gobj: setattr(g, "enabled", v == "True"), 
            expected_type=bool, 
            getter=lambda g=gobj: g.enabled
        )

        sprite = QTreeWidgetItem(["Sprite"])
        self.addTopLevelItem(sprite)
        self._add_click_field(
            "Name", 
            gobj.sprite.name, 
            lambda v, s=gobj.sprite: setattr(s, "name", v),
            self._on_set_sprite, 
            sprite
        ) 

        transform = QTreeWidgetItem(["Transform"])
        self.addTopLevelItem(transform)
        self._add_vec3(transform, "Position", gobj.transform.position)
        self._add_vec2(transform, "Scale", gobj.transform.scale)

        self.comps_tree = QTreeWidgetItem(["Components"])
        self.addTopLevelItem(self.comps_tree)
        for comp in gobj.comps:
            comp_item = QTreeWidgetItem([comp.name])
            self.comps_tree.addChild(comp_item)
            for var in comp.vars:
                self._add_comp_var(comp_item, var)

        self.expandAll()

        self.setContextMenuPolicy(Qt.CustomContextMenu) 
        self.itemChanged.connect(self._on_item_changed)
        self.customContextMenuRequested.connect(lambda pos: self.open_inspector_menu(pos))
        self.itemDoubleClicked.connect(self._on_item_click)

    def _add_edit_field(self, name, value, setter, parent=None, expected_type=str, getter=None):
        item = QTreeWidgetItem([name, str(value)])
        item.setFlags(item.flags() | Qt.ItemIsEditable)
        item.setData(1, Qt.UserRole, setter)
        item.setData(1, Qt.UserRole + 1, expected_type)
        item.setData(1, Qt.UserRole + 3, getter)
        item.setData(0, Qt.UserRole, name)
        if parent:
            parent.addChild(item)
        else:
            self.addTopLevelItem(item)
        
        return item

    def _add_click_field(self, name, value, setter, click_callback, parent=None):
        item = QTreeWidgetItem([name, str(value)])      
        item.setFlags(item.flags() & ~Qt.ItemIsEditable)
        item.setData(0, Qt.UserRole, click_callback)
        item.setData(1, Qt.UserRole, setter)
        item.setText(1, value)
        
        if parent:
            parent.addChild(item)
        else:
            self.addTopLevelItem(item)
        
        return item
    
    def _on_item_click(self, item:QTreeWidgetItem, column):
        callback = item.data(0, Qt.UserRole)
        if callable(callback): 
            callback(item)

    def _add_vec3(self, parent, name, vec):
        group = QTreeWidgetItem([name])
        parent.addChild(group)
        for axis in ("x", "y", "z"):
            self._add_edit_field(
                axis, 
                getattr(vec, axis), 
                setter = lambda v, vec=vec, axis=axis: setattr(vec, axis, float(v)), 
                parent=group, 
                expected_type=float, 
                getter=lambda vec=vec, axis=axis: getattr(vec, axis)
            )

    def _add_vec2(self, parent, name, vec):
        group = QTreeWidgetItem([name])
        parent.addChild(group)
        for axis in ("x", "y"):
            self._add_edit_field(
                axis, 
                getattr(vec, axis), 
                setter = lambda v, vec=vec, axis=axis: setattr(vec, axis, float(v)), 
                parent=group, expected_type=float, 
                getter = lambda vec=vec, axis=axis: getattr(vec, axis)
            )

    def _add_comp_var(self, item, var:scene.Var):
        match var.special:              
            case scene.SPECIAL_VAR_GOBJ:
                gobj = scene.navigate_to_gobj(self.current_gobj, var.value)
                if gobj is None: setattr(var, "value", "")

                gobj_item = self._add_click_field(
                    var.name, 
                    var.value, 
                    lambda v, var=var: setattr(var, "value", v), 
                    self._on_set_gobj,
                    item
                )
                
                gobj_item.setText(1, "" if gobj is None else gobj.name )
            case _:
                self._add_var(item, var)

    def _add_var(self, parent, var):
        value = var.value
        expected_type = type(value)
        setter = lambda v, var=var: setattr(var, "value", v)
        getter = lambda var=var: getattr(var, "value")
        self._add_edit_field(var.name, value, setter, parent=parent, expected_type=expected_type, getter=getter)
    
    def _add_var_click(self, parent, var:scene.Var, click):
        value = var.value
        setter = lambda v, var=var: setattr(var, "value", v)
        self._add_click_field(var.name, value, setter, click, parent)

    def edit(self, index, trigger, event):
        if index.column() == 0:
            return False
        return super().edit(index, trigger, event)

    def _on_item_changed(self, item: QTreeWidgetItem, column: int):

        setter = item.data(1, Qt.UserRole)
        expected_type = item.data(1, Qt.UserRole + 1)
        current_getter = item.data(1, Qt.UserRole + 3)
        
        if not setter or not expected_type or not callable(current_getter):
            return

        if column == 0:
            original = item.data(0, Qt.UserRole)
            item.setText(0, original)
            return
        
        text = item.text(1)

        try:
            converted = expected_type(text)
        except (ValueError, TypeError):
            item.setText(1, str(current_getter()))
            return
        
        setter(converted)

    def _on_set_sprite(self, item: QTreeWidgetItem):
        global global_packet

        index = 0
        name_list = list(global_packet.sprites.keys()) + ["none"]
        if self.current_gobj.sprite.name in name_list:
            index = name_list.index(self.current_gobj.sprite.name)
        else:
            index = len(name_list)

        sprite_name, ok = QInputDialog.getItem(self, "Select", "Select Sprite:", name_list, index, False)
        if not ok or not sprite_name:
            return
        
        self.current_gobj.sprite.name = sprite_name
        item.setText(1, sprite_name)

    def _on_set_gobj(self, item: QTreeWidgetItem):

        gobj_list = self.root_gobj.to_list()
        gobj_objs = [gobj for gobj, _ in gobj_list]
        gobj_names = [name for _, name in gobj_list]

        gobj_name , ok = QInputDialog.getItem(self, "Path", "Select Game Object:", gobj_names, 0, False)
        if not ok or not gobj_name:
            return
        
        setter = item.data(1, Qt.UserRole)
        
        path = scene.get_nav_to_gobj(self.current_gobj, gobj_objs[gobj_names.index(gobj_name)])
        
        setter(path)
        item.setText(1, gobj_name)

    def open_inspector_menu(self, pos:QPoint):
        item = self.itemAt(pos)
        if not item:
            return

        if item is self.comps_tree:
            self.menu_component_root(item,pos)
        elif item.parent() is self.comps_tree:
            self.menu_component_child(item,pos)
            pass

    def menu_component_root(self, item:QTreeWidgetItem, pos:QPoint):
        menu = QMenu(self)
        add = menu.addAction("Add Component")
        action = menu.exec(self.viewport().mapToGlobal(pos))

        if action == add:
            global global_loaded_comps_name

            comp_name, ok = QInputDialog.getItem(self, "Add Component", "Select Component:", global_loaded_comps_name, 0, False)
            if not ok or not comp_name:
                return
            
            if self.current_gobj.contains_comp(comp_name): return

            comp = global_get_comp_clone(comp_name)

            self.current_gobj.comps.append(comp)

            comp_item = QTreeWidgetItem([comp.name])
            self.comps_tree.addChild(comp_item)
            for var in comp.vars:
                self._add_comp_var(comp_item, var)
            
            comp_item.setExpanded(True)
            self.comps_tree.setExpanded(True)
        
    def menu_component_child(self, item:QTreeWidgetItem, pos:QPoint):
        menu = QMenu(self)
        default = menu.addAction("Default")
        delete = menu.addAction("Delete")
        action = menu.exec(self.viewport().mapToGlobal(pos))

        index = item.parent().indexOfChild(item)

        if action == default:
            clone = global_get_comp_clone(item.text(0))
            self.current_gobj.comps[index] = clone
            item.takeChildren()
            for var in clone.vars:
                self._add_comp_var(item, var)
            pass
        if action == delete:
            del self.current_gobj.comps[index]
            item.parent().removeChild(item)
            pass

class GobjTree(QTreeWidget):
    def __init__(self, prefab_view:PrefabView, inspector: GobjInspector):
        super().__init__()
        self.setColumnCount(1)
        self.setHeaderLabels(["Prefab"])
        
        self.inspector = inspector
        self.prefab = prefab_view

        self.setDragEnabled(True)
        self.setDropIndicatorShown(True)
        self.setDragDropMode(QTreeWidget.InternalMove)

        self.itemClicked.connect(lambda item: self.select_item(item))

        self.setContextMenuPolicy(Qt.CustomContextMenu) 
        self.customContextMenuRequested.connect(lambda pos: self.open_gobj_menu(pos))
        
        self.gobj_root = scene.GameObject("GameObject")
        item = scene.gobj_to_tree(self.gobj_root)
        self.insertTopLevelItem(0,item)

        self.inspector.set_gameobject(self.gobj_root, self.gobj_root, item)
    
    def set_gobj(self, gobj:scene.GameObject):
        self.gobj_root = gobj
        self.tree_view()
    
    def tree_view(self):
        self.clear()
        item = scene.gobj_to_tree(self.gobj_root)
        self.insertTopLevelItem(0,item)

        self.inspector.set_gameobject(self.gobj_root, self.gobj_root, item)

    def refreash_tree(self):
        self.clear()
        self.insertTopLevelItem(0,scene.gobj_to_tree(self.gobj_root, self.gobj_root))

    def select_item(self, item):
        gobj = item.data(0,Qt.UserRole)
        self.inspector.set_gameobject(self.gobj_root, gobj, item)

    def dropEvent(self, event: QDropEvent):
        item_drag:QTreeWidgetItem = self.currentItem()  
        gobj_drag:scene.GameObject = item_drag.data(0,Qt.UserRole)

        if gobj_drag is self.gobj_root:
            msgBox = QMessageBox()
            msgBox.setText("Cant move root gobj.")
            msgBox.exec()
            return
        
        item_target:QTreeWidgetItem = self.itemAt(event.position().toPoint())
        if item_target is None:
            msgBox = QMessageBox()
            msgBox.setText("Cant move gobj outside of root.")
            msgBox.exec()
            return

        super().dropEvent(event)

        item_drag_parent:QTreeWidgetItem = item_drag.parent()
        gobj_parent:scene.GameObject = item_drag_parent.data(0,Qt.UserRole)

        index = item_drag_parent.indexOfChild(item_drag)

        gobj_drag.parent.children.remove(gobj_drag)
        gobj_parent.children.insert(index, gobj_drag)

        item_drag_parent.setExpanded(True)

    def open_gobj_menu(self, pos: QPoint):
        item = self.itemAt(pos)
        if not item:
            self.open_prefab_menu(pos)
            return

        menu = QMenu(self)
        rename = menu.addAction("Add Child")
        delete = menu.addAction("Delete")
        action = menu.exec(self.viewport().mapToGlobal(pos))
        
        if action == rename:
            gobj_parent:scene.GameObject = item.data(0,Qt.UserRole)
            name, ok = QInputDialog.getText(self,"Create child.","Child name.")
            if not ok or name == "": return

            gobj_child = scene.GameObject(name,gobj_parent)
            gobj_parent.children.append(gobj_child)

            item.addChild(scene.gobj_to_tree(gobj_child))
            item.setExpanded(True)
        elif action == delete:
            gobj_remove:scene.GameObject = item.data(0,Qt.UserRole)
            if gobj_remove == self.gobj_root: 
                msgBox = QMessageBox()
                msgBox.setText("Cant delete root gobj.")
                msgBox.exec()
                return
            reply = QMessageBox.question(None,"Confirm",f"Delete {gobj_remove.name}?",QMessageBox.Yes | QMessageBox.No)
            if reply == QMessageBox.No: return
            gobj_remove.parent.children.remove(gobj_remove)
            item.parent().removeChild(item)
    
    def open_prefab_menu(self, pos: QPoint):
        menu = QMenu(self)
        new = menu.addAction("New")
        save = menu.addAction("Save")
        load = menu.addAction("Load")
        delete = menu.addAction("Delete")
        action = menu.exec(self.viewport().mapToGlobal(pos))

        if action == new: self.prefab.new_prefab()
        elif action == save: self.prefab.save_prefab()
        elif action == load: self.prefab.load_prefab()
        elif action == delete: self.prefab.delete_prefab()

class GobjCanvas(QWidget):
    def __init__(self):
        super().__init__()

        self.gobj:scene.GameObject = None
        self.timer = QTimer(self)
        self.timer.timeout.connect(self.update)
        self.timer.start(16)

    def set_gobj(self, gobj):
        self.gobj = gobj

    def paintEvent(self, event):
        p = QPainter(self)

        font = QFont()
        font.setPointSize(8)
        p.setFont(font)
        
        rect = self.rect()
        p.fillRect(rect, Qt.black)

        rect = self.rect()

        p.setBrush(Qt.NoBrush)
        p.setPen(Qt.blue)

        center = rect.center()

        p.drawLine(center.x(), rect.top(),
                center.x(), rect.bottom())

        p.drawLine(rect.left(),  center.y(),
                rect.right(), center.y())
        
        p.setPen(Qt.NoPen)

        if not self.gobj:
            return

        stack = [self.gobj]

        while stack:
            gobj = stack.pop()

            p.setBrush(Qt.NoBrush)
            p.setPen(Qt.white)

            gobj_center = gobj.transform.to_center_rect()
            rect_to_draw = gobj_center.translated(center)
            p.drawRect(rect_to_draw)

            if gobj.sprite.name in global_packet.sprites.keys():
                pixmap = global_packet.sprites[gobj.sprite.name].pixmap
                p.drawPixmap(rect_to_draw.toRect(), pixmap)
            
            p.setPen(Qt.white)

            text_pos = QPoint(rect_to_draw.right(), rect_to_draw.top() - 4)

            p.drawText(text_pos, gobj.name)

            stack.extend(gobj.children)

class GamePackView(QWidget):
    def __init__(self, parent:QWidget):
        super().__init__(parent)
        
        self.layout_main = QVBoxLayout(self)
        self.layout_resources = QVBoxLayout()

        self.layout_main.addLayout(self.layout_resources)

        dir_label = QLabel(text="Packet Resources")

        dir_button = QPushButton()
        dir_button.setText("Add Sprite")
        dir_button.clicked.connect(self.load_sprite_directorie)

        self.sprite_list = QListWidget()
        self.sprite_list.setObjectName("list_dir")
        self.sprite_list.itemDoubleClicked.connect(self.remove_sprite)
        self.sprite_list.setIconSize(QSize(32,32))
        
        self.layout_resources.addWidget(dir_label)
        self.layout_resources.addWidget(dir_button)
        self.layout_resources.addWidget(self.sprite_list)

    def reload_view(self):
        self.reload_sprites()

    def load_sprite_directorie(self):
        path, _ = QFileDialog.getOpenFileName(self,filter="Image PNG (*.png)")
        if path == "": return
        global_packet.sprite_save(path)
        self.reload_sprites()
        pass

    def remove_sprite(self, item:QListWidgetItem):
        global_packet.sprite_remove(item.text())
        self.reload_sprites()
        pass

    def reload_sprites(self):
        self.sprite_list.clear()
        for name, sprite in global_packet.sprites.items():
            pix = QPixmap()
            pix.loadFromData(sprite.data)
            item = QListWidgetItem(QIcon(pix),name)
            self.sprite_list.addItem(item)
    
    def load_component_directorie(self):
        global global_edata
        global global_loaded_comps
        global global_loaded_comps_name

        path = QFileDialog(directory=global_edata.components_lpath).getExistingDirectory()
        if path == "": return

        if global_edata.comp_directorie_contains(path): return
        global_edata.components_lpath = path
        global_edata.comp_directorie_add(path)

        comps = parser.parse_component_file(path)
        for comp in comps:
            if comp.name not in global_loaded_comps_name:
                global_loaded_comps.append(comp)
                global_loaded_comps_name.append(comp.name)

        self.wcomp_comp_list.addItems([c.name for c in global_loaded_comps])

        global_edata.save()
        self.update()

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        global global_edata

        self.setWindowTitle("SillyEditor")
        self.setMinimumSize(855,450)
        menu = self.menuBar()
        
        self.views:Dict[str,QWidget] = {}

        fileMenu = menu.addMenu("File")
        fileMenu.addAction("New").triggered.connect(lambda: self.new_gamepacket())
        fileMenu.addAction("Save").triggered.connect(lambda: self.save_gamepacket())
        fileMenu.addAction("Load").triggered.connect(lambda: self.load_gamepacket())
        viewMene = menu.addMenu("View")
        viewMene.addAction("Packet").triggered.connect(lambda: self.set_view("packet"))
        viewMene.addAction("Prefabs").triggered.connect(lambda: self.set_view("prefab"))
        viewMene.addAction("Components").triggered.connect(lambda: self.set_view("comp"))

        container = QWidget()
        container_layout = QVBoxLayout(container)
        container_layout.setContentsMargins(0, 0, 0, 0)
        container_layout.setSpacing(0)

        self.setCentralWidget(container)
        
        self.comp_view = ComponentView(container)
        self.views["comp"] = self.comp_view

        self.packet_view = GamePackView(container)
        self.views["packet"] = self.packet_view

        self.prefab_view = PrefabView(container)
        self.views["prefab"] = self.prefab_view

        self.set_view("packet")
        for view in self.views.values():
            container_layout.addWidget(view)
    
    def new_gamepacket(self):
        global global_packet
        global_packet = gamepack.GamePacket()
        self.reload_view_data()

    def save_gamepacket(self):
        global global_packet
        path, _ = QFileDialog.getSaveFileName(self, "Save GamePacket", "", "GamePacket Files (*.gpkt)")
        if not path:
            return

        if not path.endswith(".gpkt"):
            path += ".gpkt"
        with open(path, "w", encoding="utf-8") as f:
            json.dump(global_packet.to_dict(), f, indent=4)

    def load_gamepacket(self):
        global global_packet
        path, _ = QFileDialog.getOpenFileName(self, "Load GamePacket", "", "GamePacket Files (*.gpkt)")
        if not path:
            return
        
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
            global_packet = gamepack.GamePacket.from_dict(data)
        
        self.reload_view_data()

    def reload_view_data(self):
        self.prefab_view.reload_view()
        self.comp_view.reload_view()
        self.packet_view.reload_view()
        pass

    def set_view(self, view_id: str):
        for view in self.views.values():
            view.hide()
        self.views[view_id].show()

app = QApplication()

window = MainWindow()
window.show()

app.exec()
