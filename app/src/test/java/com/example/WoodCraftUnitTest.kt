package com.example

import com.example.data.ProjectRepository
import com.example.model.FurniturePart
import com.example.model.MeasurementUnit
import com.example.model.PartType
import com.example.model.WoodMaterial
import com.example.utils.CutListGenerator
import com.example.utils.ObjExporter
import com.example.utils.StlExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WoodCraftUnitTest {

    @Test
    fun testDefaultDeskProjectCreation() {
        val desk = ProjectRepository.createDefaultDeskProject()
        assertEquals("Computer Desk", desk.name)
        assertEquals(MeasurementUnit.CM, desk.unit)
        assertEquals(4, desk.parts.size)

        val top = desk.parts.find { it.name == "Desktop" }
        assertNotNull(top)
        assertEquals(135f, top!!.width, 0.01f)
        assertEquals(1.8f, top.height, 0.01f)
        assertEquals(60f, top.thickness, 0.01f)
    }

    @Test
    fun testCutListGeneration() {
        val desk = ProjectRepository.createDefaultDeskProject()
        val cutList = CutListGenerator.generateCutList(desk)

        // Left and Right sides have same dimensions (1.8 x 75 x 60) so they should be grouped into Qty 2
        val sideGroup = cutList.find { it.quantity == 2 }
        assertNotNull(sideGroup)
        assertTrue(sideGroup!!.partIds.size == 2)

        val exportText = CutListGenerator.exportToText(desk)
        assertTrue(exportText.contains("WOODWORKING CUTTING LIST"))
        assertTrue(exportText.contains("Qty 2"))
    }

    @Test
    fun testMeasurementUnitConversion() {
        val cm = 100f
        assertEquals("100 cm", MeasurementUnit.CM.format(cm))
        assertEquals("1000 mm", MeasurementUnit.MM.format(cm))
        assertEquals("39.37 in", MeasurementUnit.INCH.format(cm))

        val inchInCm = MeasurementUnit.INCH.parseToCm(10f)
        assertEquals(25.4f, inchInCm, 0.01f)
    }

    @Test
    fun testObjExporter() {
        val desk = ProjectRepository.createDefaultDeskProject()
        val obj = ObjExporter.exportToObj(desk)
        assertTrue(obj.contains("# WoodCraft 3D Wavefront OBJ Exporter"))
        assertTrue(obj.contains("v "))
        assertTrue(obj.contains("f "))
    }

    @Test
    fun testStlExporter() {
        val desk = ProjectRepository.createDefaultDeskProject()
        val stl = StlExporter.exportToStl(desk)
        assertTrue(stl.startsWith("solid "))
        assertTrue(stl.contains("facet normal"))
        assertTrue(stl.contains("vertex"))
        assertTrue(stl.endsWith("endsolid Computer_Desk\n"))
    }
}
