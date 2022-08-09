package com.vk.kphpstorm.configuration

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.impl.AbstractColorsScheme
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.jetbrains.php.lang.highlighter.PhpHighlightingData

// setup KPHPStorm plugin includes disabling some native inspections and enabling new ones instead
// (these identifiers are <shortName> from plugin.xml from decompiled PhpStorm sources)
private val nativeInspectionsToDisable = listOf(
        // works incorrect for 'kmixed'
        "PhpToStringImplementationInspection",
        // works incorrect for 'kmixed', 'future', 'T' etc; is replaced by KphpUndefinedClassInspection
        "PhpUndefinedClassInspection",
        // phpdocs and typing in them is fully replaced by KphpDocInspection
        "PhpDocMissingReturnTagInspection",
        "PhpDocSignatureInspection",
        "PhpDocDuplicateTypeInspection",
        "PhpDocFieldTypeMismatchInspection",
        "PhpRedundantDocCommentInspection",
        // "PHP > Type compatibility" inspections are all turned off, as they produce false positives/negatives
        "PhpParamsInspection",                      // KphpParameterTypeMismatchInspection
        "PhpWrongForeachArgumentTypeInspection",    // ArrayAndIndexingInspection
        "PhpIllegalArrayKeyTypeInspection",         // ArrayAndIndexingInspection
        "PhpIllegalStringOffsetInspection",         // ArrayAndIndexingInspection
        "PhpStrictTypeCheckingInspection",          // done by various checks
        "PhpIncompatibleReturnTypeInspection",      // KphpReturnTypeMismatchInspection
        "PhpMissingFieldTypeInspection",            // done by phpdoc checks
        "PhpMissingReturnTypeInspection",           // done by phpdoc checks
        "PhpFieldAssignmentTypeMismatchInspection", // KphpAssignmentTypeMismatchInspection
        "PhpRedundantTypeInUnionTypeInspection",    // done by various checks
        "PhpMissingParamTypeInspection",            // done by @param inspection

)

// The KPHPStorm plugin configuration includes overriding the warning level of standard inspections
private val nativeInspectionCustomLevel = mapOf(
    "PhpMethodOrClassCallIsNotCaseSensitiveInspection" to HighlightDisplayLevel.ERROR,
)

// setup KPHPStorm plugin includes "unchecking" some colors&fonts from "PHP" section
// (this is already listed in colorSchemes/*.xml, but doing this also manually is more reliable)
private val nativeTextAttributesToAnnulate = listOf(
        PhpHighlightingData.DOC_IDENTIFIER,
        PhpHighlightingData.DOC_TAG,
        PhpHighlightingData.FUNCTION_CALL
)

/**
 * Perform plugin setup for project
 * 1) Disable some native inspections but enable plugin's
 * 2) Disable some native color scheme settings, not to be mixed with KphpStormAnnotator
 *    (this is globally, not per-project, but annotator also works globally, so it is expected)
 */
internal fun setupKphpStormPluginForProject(project: Project) {
    val curInspectionProfile = ProjectInspectionProfileManager.getInstance(project).currentProfile
    for (toolState in curInspectionProfile.allTools) {
        val inspection = toolState.tool
        val shortName = inspection.shortName

        if (inspection.groupDisplayName == "KPHPStorm plugin") // <groupName>
            curInspectionProfile.setToolEnabled(shortName, true, project, false)
        if (nativeInspectionsToDisable.contains(shortName))
            curInspectionProfile.setToolEnabled(shortName, false, project, false)

        val level = nativeInspectionCustomLevel[shortName]
        if (level != null) {
            curInspectionProfile.setToolEnabled(shortName, true, project, false)

            curInspectionProfile.setErrorLevel(HighlightDisplayKey.find(shortName), level, project)
        }
    }
    // this line saves settings (without this, on/off inspections restore previous state after restart)
    ProjectInspectionProfileManager.getInstance(project).fireProfileChanged(curInspectionProfile)

    
    val curColorScheme = EditorColorsManager.getInstance().globalScheme
    val emptyText = TextAttributes(null, null, null, null, 0)
    for (attrName in nativeTextAttributesToAnnulate) {
        curColorScheme.setAttributes(attrName, emptyText)
    }
    // these lines also save settings (without them color settings restore previous state after restart)
    (curColorScheme as AbstractColorsScheme).setSaveNeeded(true)
    (EditorColorsManager.getInstance() as EditorColorsManagerImpl).schemeChangedOrSwitched(null)


    // after having made all changes, save that setup was done
    KphpStormConfiguration.saveThatSetupForProjectDone(project)
}
