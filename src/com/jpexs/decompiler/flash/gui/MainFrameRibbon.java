/*
 *  Copyright (C) 2010-2024 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;

/**
 * @author JPEXS
 */
public final class MainFrameRibbon extends AppRibbonFrame {

    private final MainPanel panel;

    private final MainFrameMenu mainMenu;

    public MainFrameRibbon() {
        super();
        

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JRibbon ribbon = getRibbon();
        cnt.add(ribbon, BorderLayout.NORTH);

        mainMenu = new MainFrameRibbonMenu(this, ribbon);
        mainMenu.createMenuBar();

        panel = new MainPanel(this, mainMenu);
        panel.setBackground(Color.yellow);
        cnt.add(panel, BorderLayout.CENTER);

        int w = Configuration.guiWindowWidth.get();
        int h = Configuration.guiWindowHeight.get();
        GraphicsDevice device = View.getMainDefaultScreenDevice();
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
        int maxWidth = bounds.width - (insets.left + insets.right);
        int maxHeight = bounds.height - (insets.top + insets.bottom);

        if (w > maxWidth) {
            w = maxWidth;
        }
        if (h > maxHeight) {
            h = maxHeight;
        }
        setSize(w, h);

        boolean maximizedHorizontal = Configuration.guiWindowMaximizedHorizontal.get();
        boolean maximizedVertical = Configuration.guiWindowMaximizedVertical.get();

        int state = 0;
        if (maximizedHorizontal) {
            state |= JFrame.MAXIMIZED_HORIZ;
        }
        if (maximizedVertical) {
            state |= JFrame.MAXIMIZED_VERT;
        }
        setExtendedState(state);

        View.setWindowIcon(this);
        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                int state = e.getNewState();
                Configuration.guiWindowMaximizedHorizontal.set((state & JFrame.MAXIMIZED_HORIZ) == JFrame.MAXIMIZED_HORIZ);
                Configuration.guiWindowMaximizedVertical.set((state & JFrame.MAXIMIZED_VERT) == JFrame.MAXIMIZED_VERT);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int state = getExtendedState();
                if ((state & JFrame.MAXIMIZED_HORIZ) == 0) {
                    Configuration.guiWindowWidth.set(getWidth());
                }
                if ((state & JFrame.MAXIMIZED_VERT) == 0) {
                    Configuration.guiWindowHeight.set(getHeight());
                }
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Configuration.saveSessionOnExit.get()) {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbt = new StringBuilder();

                    boolean first = true;
                    for (OpenableList swf : panel.getSwfs()) {
                        if (!first) {
                            sb.append(File.pathSeparator);
                            sbt.append(File.pathSeparator);
                        }
                        first = false;
                        String file = swf.sourceInfo.getFile();
                        if (file != null) {
                            sb.append(file);
                            String t = swf.sourceInfo.getFileTitle();
                            sbt.append(t == null ? "" : t);
                        }
                    }

                    Configuration.lastSessionFiles.set(sb.toString());
                    Configuration.lastSessionFileTitles.set(sbt.toString());

                    SWF easySwf = panel.getEasyPanel().getSwf();
                    if (easySwf != null) {
                        Configuration.lastSessionEasySwf.set(easySwf.getFile() + "|" + easySwf.getFileTitle());
                    } else {
                        Configuration.lastSessionEasySwf.set("");
                    }

                    String pathResources = panel.tagTree.getSelectionPathString();
                    if (pathResources != null) {
                        Configuration.lastSessionSelection.set(pathResources);
                    }
                    String pathTagList = panel.tagListTree.getSelectionPathString();
                    if (pathTagList != null) {
                        Configuration.lastSessionTagListSelection.set(pathTagList);
                    }

                }

                boolean closeResult = panel.closeAll(true, true);
                if (closeResult) {
                    Main.exit();
                }
            }
        });

        View.centerScreenMain(this);

    }

    @Override
    public void setExtendedState(int state) {
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            GraphicsConfiguration gc = View.getWindowDevice(MainFrameRibbon.this.getWindow()).getDefaultConfiguration();

            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            Rectangle screenBounds = gc.getBounds();
            Rectangle maxBounds = new Rectangle(
                    screenBounds.x + screenInsets.left,
                    screenBounds.y + screenInsets.top,
                    screenBounds.width - (screenInsets.left + screenInsets.right),
                    screenBounds.height - (screenInsets.top + screenInsets.bottom)
            );
            setMaximizedBounds(maxBounds);
        }
        super.setExtendedState(state);
    }

    private static void getApplicationMenuButtons(Component comp, List<JRibbonApplicationMenuButton> ret) {
        if (comp instanceof JRibbonApplicationMenuButton) {
            ret.add((JRibbonApplicationMenuButton) comp);
            return;
        }
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for (int i = 0; i < cont.getComponentCount(); i++) {
                getApplicationMenuButtons(cont.getComponent(i), ret);
            }
        }
    }

    @Override
    public MainPanel getPanel() {
        return panel;
    }

    @Override
    public Window getWindow() {
        return this;
    }

    @Override
    public void dispose() {
        removeAll();
        mainMenu.dispose();
        Helper.emptyObject(mainMenu);
        panel.dispose();
        Helper.emptyObject(this);
        super.dispose();
    }

    @Override
    public MainFrameMenu getMenu() {
        return mainMenu;
    }
}
