<?xml version="1.0" encoding="UTF-8"?>

<Form version="1.3" maxVersion="1.8" type="org.netbeans.modules.form.forminfo.JFrameFormInfo">
<Properties>
<Property name="defaultCloseOperation" type="int" value="3"/>
</Properties>
<SyntheticProperties>
<SyntheticProperty name="formSizePolicy" type="int" value="1"/>
<SyntheticProperty name="generateCenter" type="boolean" value="false"/>
</SyntheticProperties>
<AuxValues>
<AuxValue name="FormSettings_autoResourcing" type="java.lang.Integer" value="0"/>
<AuxValue name="FormSettings_autoSetComponentName" type="java.lang.Boolean" value="false"/>
<AuxValue name="FormSettings_generateFQN" type="java.lang.Boolean" value="true"/>
<AuxValue name="FormSettings_generateMnemonicsCode" type="java.lang.Boolean" value="false"/>
<AuxValue name="FormSettings_i18nAutoMode" type="java.lang.Boolean" value="false"/>
<AuxValue name="FormSettings_layoutCodeTarget" type="java.lang.Integer" value="1"/>
<AuxValue name="FormSettings_listenerGenerationStyle" type="java.lang.Integer" value="0"/>
<AuxValue name="FormSettings_variablesLocal" type="java.lang.Boolean" value="false"/>
<AuxValue name="FormSettings_variablesModifier" type="java.lang.Integer" value="2"/>
</AuxValues>

<Layout>
<DimensionLayout dim="0">
<Group type="103" groupAlignment="0" attributes="0">
<Group type="102" alignment="0" attributes="0">
<EmptySpace max="-2" attributes="0"/>
<Group type="103" groupAlignment="0" attributes="0">
<Component id="jScrollPane1" max="32767" attributes="0"/>
<Group type="102" attributes="0">
<Component id="jLabel1" min="-2" max="-2" attributes="0"/>
<EmptySpace max="-2" attributes="0"/>
<Component id="jTextField1" min="-2" pref="50" max="-2" attributes="0"/>
<EmptySpace max="-2" attributes="0"/>
<Component id="jButton1" min="-2" max="-2" attributes="0"/>
<EmptySpace min="0" pref="154" max="32767" attributes="0"/>
</Group>
<Component id="upnp" alignment="0" max="32767" attributes="0"/>
</Group>
<EmptySpace max="-2" attributes="0"/>
</Group>
</Group>
</DimensionLayout>
<DimensionLayout dim="1">
<Group type="103" groupAlignment="0" attributes="0">
<Group type="102" alignment="0" attributes="0">
<EmptySpace max="-2" attributes="0"/>
<Group type="103" groupAlignment="3" attributes="0">
<Component id="jLabel1" alignment="3" min="-2" max="-2" attributes="0"/>
<Component id="jTextField1" alignment="3" min="-2" max="-2" attributes="0"/>
<Component id="jButton1" alignment="3" min="-2" max="-2" attributes="0"/>
</Group>
<EmptySpace max="-2" attributes="0"/>
<Component id="upnp" min="-2" max="-2" attributes="0"/>
<EmptySpace max="-2" attributes="0"/>
<Component id="jScrollPane1" pref="159" max="32767" attributes="0"/>
<EmptySpace max="-2" attributes="0"/>
</Group>
</Group>
</DimensionLayout>
</Layout>
<SubComponents>
<Component class="javax.swing.JLabel" name="jLabel1">
<Properties>
<Property name="text" type="java.lang.String" value="Listen on port"/>
</Properties>
</Component>
<Component class="javax.swing.JTextField" name="jTextField1">
<Properties>
<Property name="text" type="java.lang.String" value="1049"/>
</Properties>
</Component>
<Component class="javax.swing.JButton" name="jButton1">
<Properties>
<Property name="text" type="java.lang.String" value="Start"/>
</Properties>
<Events>
<EventHandler event="actionPerformed" listener="java.awt.event.ActionListener" parameters="java.awt.event.ActionEvent" handler="jButton1ActionPerformed"/>
</Events>
</Component>
<Container class="javax.swing.JScrollPane" name="jScrollPane1">
<AuxValues>
<AuxValue name="autoScrollPane" type="java.lang.Boolean" value="true"/>
</AuxValues>

<Layout class="org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout"/>
<SubComponents>
<Component class="javax.swing.JTextArea" name="log">
<Properties>
<Property name="editable" type="boolean" value="false"/>
<Property name="columns" type="int" value="20"/>
<Property name="rows" type="int" value="5"/>
<Property name="border" type="javax.swing.border.Border" editor="org.netbeans.modules.form.editors2.BorderEditor">
<Border info="null"/>
</Property>
</Properties>
</Component>
</SubComponents>
</Container>
<Component class="javax.swing.JCheckBox" name="upnp">
<Properties>
<Property name="selected" type="boolean" value="true"/>
<Property name="text" type="java.lang.String" value="uPnP NAT Port Forwarding"/>
</Properties>
</Component>
</SubComponents>
</Form>