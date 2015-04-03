## Developing a New Cell Component (aka Capability) ##
**by Jordan Slott**

### Purpose ###

In this tutorial, you will learn how to develop new cell components. A cell component is add-on functionality that can be attached to any kind of cell. The [Using Capabilities](http://faithhold.dyndns.org:8080/JSPWiki/Wiki.jsp?page=Using%20Capabilities) tutorial describes cell components in more detail from the user's perspective.

|**Note:** Cell components are termed _capabilities_ in the Wonderland client: the name _component_ was used by the Wonderland core team before adding support for them in the UI. The team felt _capability_ was a more apt term for users, however kept the name _component_ on the programmatic level. The remainder of this tutorial uses the _component_ terminology.|
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

This tutorial is designed for the Open Wonderland v0.5 Release.

In this tutorial you will develop the "tooltip" cell component. This component displays a configurable tooltip message when a user's mouse hovers over a cell which has this component attached. You can find the entire source for this module in the "stable" section of the Open Wonderland modules workspace. For instructions on downloading this workspace, see [Download, Build, and Deploy Project Wonderland v0.5 Modules](DownloadBuildModules05.md).

<p align='center'>
<b>Expected Duration: 60 minutes</b>
</p>

#### Prerequisites ####

Before completing this tutorial you should have already completed the following:

  * [Project Wonderland v0.5: Developing a New Cell (Parts 1 - 4)](http://openwonderland.googlecode.com/svn/www/wiki/backup/pdf/ProjectWonderlandDevelopingNewCell05Part1.pdf)
  * [Using Capabilities](http://faithhold.dyndns.org:8080/JSPWiki/Wiki.jsp?page=Using%20Capabilities)

### Cell Component Architecture ###

Figure 1 illustrates Wonderland's cell component architecture. There is an aggregation relationship between a Cell and a number of Cell Components that provide more functionlity to that Cell.

![http://faithhold.dyndns.org:8080/JSPWiki/attach/Sample%20Component%20Tutorial/cell_cellcomponent.png](http://faithhold.dyndns.org:8080/JSPWiki/attach/Sample%20Component%20Tutorial/cell_cellcomponent.png)

Every cell may have zero or more cell components attached, once attached they add to the functionality of the cell. Similar to the architecture of cells themselves, cell components have a server piece, a client piece, a server state piece, and a client state piece.

Table 1 illustrates the objects and interfaces for the cell component mechanism and their analogous cell object and interface.

| **Package** | **Cell class**       | **CellComponent class**      |
|:------------|:---------------------|:-----------------------------|
| Client        | Cell                  | CellComponent                  |
| Common   | CellClientState  | CellComponentClientState |
| Common   | CellServerState | CellComponentServerState |
| Server       | CellMO             | CellComponentMO             |

Note that Components can also have associated CellComponentFactorySPI classes, and make use of server and client [plugins](http://openwonderland.googlecode.com/svn/www/wiki/backup/pdf/ProjectWonderlandWritingPlugin05.pdf). Additionally a component will have a properties panel to be used within the object editor, and might have an associated display class (see the use of TooltipCellComponentProperties below).

On the server, a CellComponentMO can either be "live" or "not live", similar to the state of CellMO. A live CellComponentMO indicates that the CellMO in which it is attached is present in the world. When a CellMO transitions from the "not live" to "live" state (or vice versa), it iterates through all of its attached CellComponentMO objects and sets their "live" state accordingly.

Every CellMO can have at most one CellComponentMO of a particular type, as determined by the java.lang.Class of the CellComponentMO object. A list of (Darkstar references to) all components may be obtained via the CellMO.getAllComponentRefs() method; an individual component is obtained via the getComponent() method by giving a java.lang.Class. The CellMO object also supports listeners on changes to the attached components on it via the ComponentChangeListenerSrv interface.

### Adding Cell Components to a Cell ###

There are two ways components are programmatically added to Cells.

  1. Via the CellMO.addComponent() method, if the component does not already exist on the Cell. After being added, a ComponentChangeListenerSrv method (with type ChangeType.ADDED) is delivered to all listeners and the component is made "live" if the Cell is also "live". A message is sent to all clients that the component has been added to the Cell; each client will then add the corresponding client-side component object to its client-side Cell object. When users add components via the GUI (i.e. via the Object Editor), a message is sent from the client to invoke CellMO.addComponent(). To remove a component, developers can use the CellMO.removeComponent() method.
  1. Via dependency injection and the @DependsOnCellComponentMO and @UsesCellComponentMO annotations.

By using two simple annotations, developers can specify that a cell component should be automatically added to a cell when the cell is created. Here, the tooltip cell component is added to a cell:

```
@DependsOnCellComponentMO(TooltipCellComponentMO.class)
public class MyCellMO extends CellMO {
```

If you also wish to have a reference to the instance of the CellComponentMO object, you can use the @UsesCellComponentMO annotation instead. Note that in this second case, you do not need the @DependsOnCellComponentMO annotation, however, there is no harm if it also is present:

```
public class MyCellMO extends CellMO {
	@UsesCellComponentMO(TooltipCellComponentMO.class)
      private ManagedReference<TooltipCellComponentMO> tooltipMO;
}
```

Cell components themselves may also use the @DependsOnCellComponentMO and @UsesCellComponentMO annotations if they depend upon other cell components. Note, however, that support for this use case is not complete: while dependent cell components are added to the cell, they are not removed if the declaring cell component is removed from the cell.

### "Special" Cell Components ###

Cell components are used by Wonderland itself to implement core functionality. These component are often automatically added to cells. For example, the system automatically attaches a special "channel" Cell Component that enables communication between the client and server Cell objects. A table of core cell components is given below:

|**Server Class Name**|**Description**|
|:--------------------|:--------------|
|ChannelComponentMO|Enables the communication of messages between the client and server Cell objects.|
|MovableComponentMO|Enables the client to transform the position, rotation, and scaling of a Cell.|
|MovableAvatarComopnentMO|Enables the client to communicate a change in position for an avatar Cell.|
|ProximityComponentMO|Notifies listeners when avatars enter or leave a bounds around a Cell.|
|CellPhysicsPropertiesComponentMO|Maintains physics-related properties for a Cell.|

### Developing the Tooltip Cell Component ###

The tooltip cell component allows users to add tooltips to cells: when a user's mouse hovers over a cell a text message is displayed in a small window. The tooltip disappears when the user subsequently moves his or her mouse (Figure 1). The use of the tooltip cell component is further described in the Tooltips in Wonderland http://blogs.openwonderland.org/2010/01/25/tooltips-in-wonderland/ blog.

The tooltip component source code is found in the "stable" section of the wonderland-modules source code repository.

Rather than take you step-by-step through developing this module from scratch, this tutorial assumes you have the complete source code and will highlight the important parts.

### The Tooltip Component Server and Client State ###

Much like a Cell, a Cell Component also has both a server and client state that serves a similar purpose. The server state class must extend CellComponentServerState and is annotated with JAXB annotations so that its state is written to XML in the Wonderland File System (WFS) when a snapshot of the world is taken. Because the tooltip's state is stored along with the cell's XML description in a world snapshot, the cell component will be automatically added to the cell and its state set when a world snapshot is loaded.

The tooltip's server state consists of the string text and the timeout used to hide the tooltip after it hovers over a cell for some number of milliseconds.

```
@XmlRootElement(name="tooltip-cell-component")
@ServerState
public class TooltipCellComponentServerState extends CellComponentServerState {

    @XmlElement(name = "text")
    private String text = null;

    @XmlElement(name = "timeout")
    private int timeout = -1;

    /** Default constructor is needed for JAXB */
    public TooltipCellComponentServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.tooltip.server.TooltipCellComponentMO";
    }

    @XmlTransient public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    @XmlTransient public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
}
```

The getServerComponentClassName() method is the string name of the server-side CellComponentMO class. Since the server state class resides in the common package and cannot depend upon the server-side code, it must specify the server-side class name as a hard-coded string.

The tooltip's client state is almost identical to its server state, without the need for the JAXB annotations.

### The Tooltip Server-Side CellComponentMO Class ###

The TooltipCellComponentMO class extends the CellComponentMO base class and stores the state of the cell component. Instances of this class are kept by the cell in its CellMO object and managed by the Darkstar transactional middleware layer. It's similar in spirit to CellMO classes.

```
public class TooltipCellComponentMO extends CellComponentMO {

    private String text = null;
    private int timeout = -1;
    
    public TooltipCellComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.tooltip.client.TooltipCellComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {

        if (state == null) {
            state = new TooltipCellComponentClientState();
        }
        ((TooltipCellComponentClientState) state).setText(text);
        ((TooltipCellComponentClientState) state).setTimeout(timeout);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        
        if (state == null) {
            state = new TooltipCellComponentServerState();
        }
        ((TooltipCellComponentServerState) state).setText(text);
        ((TooltipCellComponentServerState) state).setTimeout(timeout);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        text = ((TooltipCellComponentServerState) state).getText();
        timeout = ((TooltipCellComponentServerState) state).getTimeout();
    }
}
```

The getClientClass() method returns the subclass of CellComponent that represents the client-side cell component class. Since TooltipCellComponentMO exists in the server package, it cannot depend upon the client package and must hard-code the client class name.

The getServerState() method fills a given CellComponentServerState object (if null, then this method should create an instance of TooltipCellComponentServerState). The setServerState() stores the state given by the server state object into the TooltipCellComponentMO object. In this example, the individual state values (text and timeout) are stored in their own variables, however simply keeping a reference to the state object is a valid alternative.

The getClientState() method fills a given CellComponentClientState object (if null, then this method should create an instance of TooltipCellComponentClientState).

### The Tooltip Client-Side CellComponent Class ###

The classes that implement the client-side functionality are the most complicated aspect of the tooltip component. Every cell component has a client-side class that extends CellComponent and keeps the state of the cell component.

```
public class TooltipCellComponent extends CellComponent {

    private String text = null;
    private int timeout = -1;

    public TooltipCellComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        text = ((TooltipCellComponentClientState) clientState).getText();
        timeout = ((TooltipCellComponentClientState) clientState).getTimeout();
    }

    public String getText() { return text; }
    public int getTimeout() { return timeout; }
}
```

The setClientState() method takes an instance of the CellComponentClientState object and stores the two attributes (text, timeout) in separate variables.

Every cell component must also implement the CellComponentFactorySPI interface if, as in the case of the tooltip component, it should appear in the list of available components in the Object Editor so that users may select them and add them to cells. (However, for cell component that shouldn't be explicitly added by users, for example the channel and moveable components, should not have a cell component factory class.

```
@CellComponentFactory
public class TooltipCellComponentFactory implements CellComponentFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tooltip/client/resources/Bundle");

    public String getDisplayName() { return BUNDLE.getString("Tooltip_Cell_Component"); }
    public String getDescription() { return BUNDLE.getString("Tooltip_Cell_Component_Description"); }

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        TooltipCellComponentServerState state = new TooltipCellComponentServerState();
        return (T) state;
    }
}
```


The getDisplayName() and getDescription() methods return the text strings that appear in the Object Editor when adding new components to cells. These strings are found in resource files, so they may be localized for different languages. The getDefaultCellComponentServerState() method returns an instance of the CellComponentServerState object that can be sent to the server to tell it how to create the proper CellComponentMO with a valid default state.

The "Add" dialog for the tooltip component can be seen in the following screen shot:

![http://faithhold.dyndns.org:8080/JSPWiki/attach/Sample%20Component%20Tutorial/add_tooltip_component.png](http://faithhold.dyndns.org:8080/JSPWiki/attach/Sample%20Component%20Tutorial/add_tooltip_component.png)


### Displaying the Tooltip On Cell Hover ###

The SceneManager singleton object in the Wonderland client delivers input-related events to interested modules. One such input event it detects is "hover". The tooltip module installs a client plugin to setup a listener on the SceneManager for hover events. (For an explanation on developing "plugins" for Wonderland, see [Writing Plugins](http://openwonderland.googlecode.com/svn/www/wiki/backup/pdf/ProjectWonderlandWritingPlugin05.pdf)).

The tooltip client plugin is defined as follows:

```
@Plugin
public class TooltipClientPlugin extends BaseClientPlugin {

    private TooltipHoverListener listener = null;
    private HUDComponent hudComponent = null;
    private TooltipJPanel tooltipPanel = null;

    @Override
    public void initialize(ServerSessionManager sessionManager) {
        listener = new TooltipHoverListener();
        super.initialize(sessionManager);
    }

    @Override
    protected void activate() {
        SceneManager.getSceneManager().addSceneListener(listener);
    }

    @Override
    protected void deactivate() {        
        SceneManager.getSceneManager().removeSceneListener(listener);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        mainHUD.removeComponent(hudComponent);
        hudComponent = null;
        tooltipPanel = null;
    }
}
```

As with all plugins, the TooltipClientPlugin class is annotated with @Plugin and overrides the initialize(), activate(), and deactivate() methods. The SceneManager event is added and removed in activate() and deactivate(), respectively. Upon deactivate(), the HUDComponent that displays the tooltip is also removed, if it exists (so that, if by chance, a tooltip is displayed while you navigate away or transport from your current world to another).

The TooltipHoverListener reacts to hover events delivered by the SceneManager, and is an inner class to TooltipClientPlugin (so that is sees the hudComponent and tooltipPanel member variables).

```
    private class TooltipHoverListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() { return new Class[] { HoverEvent.class }; }

        @Override
        public void commitEvent(Event event) {
            // Upon a Hover event, see if it is a hover start or stop. If start,
            // then see if the Cell has a Tooltip Cell Component and if so fetch
            // the text, and display the HUD Component in the position of the
            // mouse event
            HoverEvent hoverEvent = (HoverEvent)event;
            Cell cell = hoverEvent.getPrimaryCell();

            // If there is no Cell or if the hover event has ended, then just
            // hide the HUD Component.
            if (cell == null || hoverEvent.isStart() == false) {
                if (hudComponent != null) {
                    hideTooltipHUDComponent();
                }
                return;
            }

            // Fetch the Tooltip Cell Component. If there is none, then hide
            // the component for good measure.
            TooltipCellComponent comp = cell.getComponent(TooltipCellComponent.class);
            if (comp == null) {
                if (hudComponent != null) {
                    hideTooltipHUDComponent();
                }
                return;
            }

            // Otherwise, show the hud at the current mouse position with the
            // given text. We need to adjust for the fact that the position
            // returned in the mouse event has y = 0 at the top, where for the
            // HUD, y = 0 is at the bottom.
            Canvas canvas = JmeClientMain.getFrame().getCanvas();
            MouseEvent mouseEvent = hoverEvent.getMouseEvent();
            Point location = mouseEvent.getPoint();
            location.y = canvas.getHeight() - location.y;
            String text = comp.getText();
            showTooltipHUDComponent(comp, text, location);

        }

        private void hideTooltipHUDComponent() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    hudComponent.setVisible(false);
                    hud.removeComponent(hudComponent);
                    hudComponent = null;
                }
            });
        }

        private void showTooltipHUDComponent(final TooltipCellComponent component, final String text, final Point point) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // If there is an existing tooltip hud component, then set
                    // it to invisible, remove it from the hud.
                    HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    if (hudComponent != null) {
                        hudComponent.setVisible(false);
                        hud.removeComponent(hudComponent);
                        hudComponent = null;
                    }

                    // Create the hud component with the panel to display the
                    // text
                    tooltipPanel = new TooltipJPanel();
                    tooltipPanel.setText((text != null) ? text : "");
                    hudComponent = hud.createComponent(tooltipPanel);
                    hudComponent.setName("Tooltip");
                    hudComponent.setDecoratable(false);
                    hudComponent.setVisible(false);
                    hudComponent.setLocation(point);
                    hud.addComponent(hudComponent);

                    // We set it visible. If we wish to add a timeout, then
                    // we immediately set it to invisible after the given
                    // delay.
                    hudComponent.setVisible(true);
                    int timeout = component.getTimeout();
                    if (timeout != -1) {
                        hudComponent.setVisible(false, timeout);
                    }
                }
            });
        }
    }
```

The hover event is delivered to the commit() method, whose argument is HoverEvent. Its getPrimaryCell() method returns the Cell over which the mouse hovers; its isStart() method returns true if the hover has just begun and false if the hover has just ended. The remainder of the commit() method fetches text from the TooltipComponent object that exists on the Cell and converts the (x, y) position in the mouse event into the coordinates relative to the canvas in which the 3D Wonderland world is drawn.

The showTooltipHUDComponent() and hideTooltipHUDComponent() methods show and hide the tooltip, respectively. They use the TooltipJPanel class that simply displays text in a JLabel. Since this code interacts with Swing, all display updates must be performed on the AWT Event Dispatch Thread, by invoking the SwingUtilities.invokeLater() method.

The tooltip is displayed in the world using the Wonderland HUD (Heads-up Display) and setting its specific location using the HUDComponent.setLocation() method. For more information on using the HUD, please see [Sample HUD Tutorial](http://faithhold.dyndns.org:8080/JSPWiki/Wiki.jsp?page=Sample%20HUD%20Tutorial). Note that the timeout associated with the tooltip is handed entirely by the HUD.

### The Tooltip Properties Panel ###

To set component-specific properties, such as the text and timeout in the tooltip component, the Object Editor can display a custom JPanel defined in the component's module. Classes that provide such properties panels are annotated with @PropertiesFactory and implement the PropertiesFactorySPI interface. While the PropertiesFactorySPI class and the property's JPanel can be two separate classes, the tooltip component combines them into a single class, TooltipCellComponentProperties.

Parts of the TooltipCellComponentProperties class is given below:

```
@PropertiesFactory(TooltipCellComponentServerState.class)
public class TooltipCellComponentProperties extends JPanel implements PropertiesFactorySPI {

    private CellPropertiesEditor editor = null;

    public String getDisplayName() { return BUNDLE.getString("Tooltip_Cell_Component"); }
    public JPanel getPropertiesJPanel() { return this; }
    public void setCellPropertiesEditor(CellPropertiesEditor editor) { this.editor = editor; }
}
```

The getPropertiesJPanel() method returns the property JPanel to display, in this case, the same object. The setCellPropertiesEditor() method is used to inject an instance of the CellPropertiesEditor class that represents the editor window. The CellPropertiesEditor object has methods to fetch the current Cell being edited, its CellServerState object, and methods to manage property updates for each component, as discussed below.

Every properties panel has a life-cycle that mimics the actions a user can take with the Object Editor GUI. The PropertiesFactorySPI interface has methods to be implemented by the component developer to handle each stage in the property panel life-cycle. The four life-cycle stages -- open, close, restore, and apply -- are described in the table below.

|**Lifecycle stage method**|**Description**|
|:-------------------------|:--------------|
|open()|Called when a Cell is selected for editing the Object Editor GUI, and after apply() so that panels may refresh themselves. Property panels should update their GUIs to reflect the most recent values. When open() is called, the getCellServerState() method always returns the most recent state of the selected Cell.|
|close()|Called when a Cell is no longer being edited in the Object Editor GUI. Property panels should perform any necessary cleanup in this method.|
|restore()|Called when the values of a component are restored to their last applied values, typically when the user hits the Restore button. Property panels should update their GUIs to reflect the last applied values.|
|apply()|Called when the edited values of a component should be saved, typically when the user hits the Apply button. Property panels can use the addToUpdateList() method in the CellPropertiesEditor object to indicate new values have been set for a component's properties.|

For the tooltip component, the open() method is implemented below. It simply fetches the values from the TooltipCellComponentServerState, stores away the original values, and updates the GUI (a text area and timeout spinner).

```
    public void open() {
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(TooltipCellComponentServerState.class);
        if (state != null) {
            TooltipCellComponentServerState tss = (TooltipCellComponentServerState) compState;

            originalText = tss.getText();
            tooltipTextArea.setText(originalText);

            originalTimeout = tss.getTimeout();
            if (originalTimeout == -1) {
                timeoutCheckbox.setSelected(false);
                timeoutSpinner.setEnabled(false);
                timeoutSpinner.setValue((Integer) DEFAULT_TIMEOUT);
            }
            else {
                timeoutCheckbox.setSelected(true);
                timeoutSpinner.setEnabled(true);
                timeoutSpinner.setValue((Integer) originalTimeout);
            }
        }
    }
```

Since the tooltip does not need to perform any cleanup when the property panel is closed, the close() method is left empty:

```
    public void close() {
        // Do nothing for now.
    }
```

The apply() method for the tooltip component must fetch the current values in the GUI, update the TooltipCellComponentServerState with these new values, and tell the CellPropertiesEditor that its properties have changed.

```
    public void apply() {
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(TooltipCellComponentServerState.class);

        ((TooltipCellComponentServerState) compState).setText(tooltipTextArea.getText());

        int timeout = -1;
        if (timeoutCheckbox.isSelected() == true) {
            timeout = (Integer) timeoutSpinner.getValue();
        }
        ((TooltipCellComponentServerState) compState).setTimeout(timeout);

        editor.addToUpdateList(compState);
    }
```

The addToUpdateList() method takes an instance of a CellComponentServerState object; when the new state of a Cell and its components are transmitted to the server, only the state of those components that have changed are included.

The restore() method simply updates the GUI with the original values (text, timeout):

```
    public void restore() {
        tooltipTextArea.setText(originalText);
        if (originalTimeout == -1) {
            timeoutCheckbox.setSelected(false);
            timeoutSpinner.setEnabled(false);
            timeoutSpinner.setValue((Integer) DEFAULT_TIMEOUT);
        }
        else {
            timeoutCheckbox.setSelected(true);
            timeoutSpinner.setEnabled(true);
            timeoutSpinner.setValue((Integer) originalTimeout);
        }
    }
```

One final note about The TooltipCellComponentProperties class. The Object Editor enables and disables the Apply and Restore buttons based upon whether property values have actually been modified in the GUI. In order for this to work, each property sheet must tell it when values in its panel have been modified. For example, the following DocumentListener is placed on the text field containing the tooltip text:

```
    class InfoTextFieldListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) { checkDirty(); }
        public void removeUpdate(DocumentEvent e) { checkDirty(); }
        public void changedUpdate(DocumentEvent e) { checkDirty(); }

        private void checkDirty() {
            String name = tooltipTextArea.getText();
            if (editor != null && name.equals(originalText) == false) {
                editor.setPanelDirty(TooltipCellComponentProperties.class, true);
            }
            else if (editor != null) {
                editor.setPanelDirty(TooltipCellComponentProperties.class, false);
            }
        }
    }
```

Here, the current text in the JTextArea is compared with the original text. If different, then CellPropertyEditor.setPanelDirty() is passed true, otherwise, it is passed false. Note that the first argument of the setPanelDirty() method takes the Class for the property sheet; the Object Editor keeps tracks of the "dirty" state for each property sheet individually so that is may properly enable and disable buttons such as Apply and Restore.