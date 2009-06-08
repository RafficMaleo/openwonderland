/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AvatarDetailsFrame.java
 *
 * Created on May 22, 2009, 3:01:04 PM
 */

package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import imi.character.CharacterAttributes;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.ConfigType;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.HairColorConfigElement;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.PantsColorConfigElement;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.ShirtColorConfigElement;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.ShoeColorConfigElement;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarAttributes.SkinColorConfigElement;

/**
 *
 * @author jkaplan
 */
public class AvatarDetailsFrame extends javax.swing.JFrame {
    private final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private final Cursor normalCursor = Cursor.getDefaultCursor();

    private AvatarImiJME avatar;
    private String name;
    private WonderlandAvatarAttributes attributes;

    /** Creates new form AvatarDetailsFrame */
    public AvatarDetailsFrame(AvatarImiJME avatar, String name,
                              WonderlandAvatarAttributes attributes)
    {
        this.avatar = avatar;
        this.name = name;
        this.attributes = attributes;

        initComponents();

        // set the initial avatar
        apply();
    }

    protected void next(ConfigType type) {
        int index = attributes.getElementIndex(type);
        index++;
        index %= attributes.getElementCount(type);

        System.out.println("[AvatarDetailsFrame] " + type + ": " + index +
                           " / " + attributes.getElementCount(type));
        attributes.setElement(type, index);
        apply();
    }

    protected void prev(ConfigType type) {
        int index = attributes.getElementIndex(type);
        index--;
        if (index < 0) {
            index = attributes.getElementCount(type) - 1;
        }

        System.out.println("[AvatarDetailsFrame] " + type + ": " + index +
                           " / " + attributes.getElementCount(type));
        attributes.setElement(type, index);
        apply();
    }

    protected void apply() {
        final JFrame f = this;
        f.setCursor(waitCursor);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Cell cell = avatar.getCell();

                WlAvatarCharacter avatarCharacter;

                CharacterAttributes ca = attributes.getCharacterAttributes();
                WonderlandSession session = cell.getCellCache().getSession();
                ServerSessionManager manager = session.getSessionManager();
                String serverHostAndPort = manager.getServerNameAndPort();
                ca.setBaseURL("wla://avatarbaseart@"+serverHostAndPort+"/");

                LoadingInfo.startedLoading(cell.getCellID(), name);
                try {
                    WorldManager wm = ClientContextJME.getWorldManager();
                    avatarCharacter = new WlAvatarCharacter(ca, wm);
                } finally {
                    LoadingInfo.finishedLoading(cell.getCellID(), name);
                }

                avatar.changeAvatar(avatarCharacter);
                f.setCursor(normalCursor);
           }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        headPrevB = new javax.swing.JButton();
        headNextB = new javax.swing.JButton();
        closeB = new javax.swing.JButton();
        skinColorB = new javax.swing.JButton();
        hairPrevB = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        hairNextB = new javax.swing.JButton();
        hairColorB = new javax.swing.JButton();
        torsoPrevB = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        torsoNextB = new javax.swing.JButton();
        shirtColorB = new javax.swing.JButton();
        jacketPrevB = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jacketNextB = new javax.swing.JButton();
        legsPrevB = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        legsNextB = new javax.swing.JButton();
        pantsColorB = new javax.swing.JButton();
        feetPrevB = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        feetNextB = new javax.swing.JButton();
        shoesColorB = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        handsPrevB = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        handsNextB = new javax.swing.JButton();
        eyeColorB = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Configure Avatar:");

        jLabel2.setText("Hair");

        headPrevB.setText("Previous");
        headPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headPrevBActionPerformed(evt);
            }
        });

        headNextB.setText("Next");
        headNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headNextBActionPerformed(evt);
            }
        });

        closeB.setText("Close");
        closeB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeBActionPerformed(evt);
            }
        });

        skinColorB.setText("Skin Color...");
        skinColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinColorBActionPerformed(evt);
            }
        });

        hairPrevB.setText("Previous");
        hairPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hairPrevBActionPerformed(evt);
            }
        });

        jLabel3.setText("Head");

        hairNextB.setText("Next");
        hairNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hairNextBActionPerformed(evt);
            }
        });

        hairColorB.setText("Hair Color...");
        hairColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hairColorBActionPerformed(evt);
            }
        });

        torsoPrevB.setText("Previous");
        torsoPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                torsoPrevBActionPerformed(evt);
            }
        });

        jLabel4.setText("Torso");

        torsoNextB.setText("Next");
        torsoNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                torsoNextBActionPerformed(evt);
            }
        });

        shirtColorB.setText("Shirt Color...");
        shirtColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shirtColorBActionPerformed(evt);
            }
        });

        jacketPrevB.setText("Previous");
        jacketPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jacketPrevBActionPerformed(evt);
            }
        });

        jLabel5.setText("Jacket");

        jacketNextB.setText("Next");
        jacketNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jacketNextBActionPerformed(evt);
            }
        });

        legsPrevB.setText("Previous");
        legsPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legsPrevBActionPerformed(evt);
            }
        });

        jLabel6.setText("Legs");

        legsNextB.setText("Next");
        legsNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legsNextBActionPerformed(evt);
            }
        });

        pantsColorB.setText("Pants Color...");
        pantsColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pantsColorBActionPerformed(evt);
            }
        });

        feetPrevB.setText("Previous");
        feetPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feetPrevBActionPerformed(evt);
            }
        });

        jLabel7.setText("Feet");

        feetNextB.setText("Next");
        feetNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feetNextBActionPerformed(evt);
            }
        });

        shoesColorB.setText("Shoe Color...");
        shoesColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shoesColorBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 24, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 259, Short.MAX_VALUE)
        );

        handsPrevB.setText("Previous");
        handsPrevB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handsPrevBActionPerformed(evt);
            }
        });

        jLabel8.setText("Hands");

        handsNextB.setText("Next");
        handsNextB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handsNextBActionPerformed(evt);
            }
        });

        eyeColorB.setText("Eye Color...");
        eyeColorB.setActionCommand("Eye Color...");
        eyeColorB.setEnabled(false);
        eyeColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eyeColorBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(headPrevB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel3))
                            .add(layout.createSequentialGroup()
                                .add(hairPrevB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel2))
                            .add(layout.createSequentialGroup()
                                .add(torsoPrevB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel4))
                            .add(layout.createSequentialGroup()
                                .add(jacketPrevB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel5))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(feetPrevB)
                                    .add(legsPrevB)
                                    .add(handsPrevB))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel6)
                                    .add(jLabel8)
                                    .add(jLabel7))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(headNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(hairNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(torsoNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jacketNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(handsNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(legsNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(eyeColorB)
                                    .add(pantsColorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(hairColorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(skinColorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(shirtColorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))))
                            .add(layout.createSequentialGroup()
                                .add(feetNextB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(shoesColorB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, closeB))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel2)
                                .add(hairPrevB))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(hairNextB)
                                .add(hairColorB)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel3)
                                .add(headPrevB))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(headNextB)
                                .add(skinColorB)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel4)
                                .add(torsoPrevB))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(torsoNextB)
                                .add(shirtColorB)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel5)
                                .add(jacketPrevB))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jacketNextB)
                                .add(eyeColorB)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel8)
                                .add(handsPrevB))
                            .add(handsNextB))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel6)
                                .add(legsPrevB))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(legsNextB)
                                .add(pantsColorB)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel7)
                                    .add(feetPrevB)))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(feetNextB)
                                    .add(shoesColorB))))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(closeB)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void headNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headNextBActionPerformed
        next(ConfigType.HEAD);
    }//GEN-LAST:event_headNextBActionPerformed

    private void closeBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeBActionPerformed

    private void skinColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinColorBActionPerformed
        Color skinColor = JColorChooser.showDialog(this, "Choose sking color", 
                                                   null);

        SkinColorConfigElement skin = new SkinColorConfigElement();
        skin.setR(skinColor.getRed() / 255.0f);
        skin.setG(skinColor.getGreen() / 255.0f);
        skin.setB(skinColor.getBlue() / 255.0f);
        attributes.setElement(ConfigType.SKIN_COLOR, skin);
        apply();
    }//GEN-LAST:event_skinColorBActionPerformed

    private void hairPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hairPrevBActionPerformed
        prev(ConfigType.HAIR);
}//GEN-LAST:event_hairPrevBActionPerformed

    private void hairNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hairNextBActionPerformed
        next(ConfigType.HAIR);
}//GEN-LAST:event_hairNextBActionPerformed

    private void hairColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hairColorBActionPerformed
        Color hairColor = JColorChooser.showDialog(this, "Choose hair color",
                                                   null);

        HairColorConfigElement hair = new HairColorConfigElement();
        hair.setR(hairColor.getRed() / 255.0f);
        hair.setG(hairColor.getGreen() / 255.0f);
        hair.setB(hairColor.getBlue() / 255.0f);
        attributes.setElement(ConfigType.HAIR_COLOR, hair);
        apply();
}//GEN-LAST:event_hairColorBActionPerformed

    private void headPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headPrevBActionPerformed
        prev(ConfigType.HEAD);
    }//GEN-LAST:event_headPrevBActionPerformed

    private void torsoPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_torsoPrevBActionPerformed
        prev(ConfigType.TORSO);
}//GEN-LAST:event_torsoPrevBActionPerformed

    private void torsoNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_torsoNextBActionPerformed
        next(ConfigType.TORSO);
}//GEN-LAST:event_torsoNextBActionPerformed

    private void shirtColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shirtColorBActionPerformed
        Color shirtColor = JColorChooser.showDialog(this, "Choose shirt color",
                                                    null);

        ShirtColorConfigElement shirt = new ShirtColorConfigElement();
        shirt.setR(shirtColor.getRed() / 255.0f);
        shirt.setG(shirtColor.getGreen() / 255.0f);
        shirt.setB(shirtColor.getBlue() / 255.0f);
        attributes.setElement(ConfigType.SHIRT_COLOR, shirt);
        apply();
}//GEN-LAST:event_shirtColorBActionPerformed

    private void jacketPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jacketPrevBActionPerformed
        prev(ConfigType.JACKET);
}//GEN-LAST:event_jacketPrevBActionPerformed

    private void jacketNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jacketNextBActionPerformed
        next(ConfigType.JACKET);
}//GEN-LAST:event_jacketNextBActionPerformed

    private void legsPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legsPrevBActionPerformed
        prev(ConfigType.LEGS);
}//GEN-LAST:event_legsPrevBActionPerformed

    private void legsNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legsNextBActionPerformed
        next(ConfigType.LEGS);
}//GEN-LAST:event_legsNextBActionPerformed

    private void pantsColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pantsColorBActionPerformed
        Color pantsColor = JColorChooser.showDialog(this, "Choose pants color",
                                                    null);

        PantsColorConfigElement pants = new PantsColorConfigElement();
        pants.setR(pantsColor.getRed() / 255.0f);
        pants.setG(pantsColor.getGreen() / 255.0f);
        pants.setB(pantsColor.getBlue() / 255.0f);
        attributes.setElement(ConfigType.PANTS_COLOR, pants);
        apply();
}//GEN-LAST:event_pantsColorBActionPerformed

    private void feetPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feetPrevBActionPerformed
        prev(ConfigType.FEET);
}//GEN-LAST:event_feetPrevBActionPerformed

    private void feetNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feetNextBActionPerformed
        next(ConfigType.FEET);
}//GEN-LAST:event_feetNextBActionPerformed

    private void shoesColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shoesColorBActionPerformed
        Color shoesColor = JColorChooser.showDialog(this, "Choose shoes color",
                                                    null);

        ShoeColorConfigElement shoes = new ShoeColorConfigElement();
        shoes.setR(shoesColor.getRed() / 255.0f);
        shoes.setG(shoesColor.getGreen() / 255.0f);
        shoes.setB(shoesColor.getBlue() / 255.0f);
        attributes.setElement(ConfigType.SHOE_COLOR, shoes);
        apply();
}//GEN-LAST:event_shoesColorBActionPerformed

    private void handsPrevBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handsPrevBActionPerformed
        prev(ConfigType.HANDS);
}//GEN-LAST:event_handsPrevBActionPerformed

    private void handsNextBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handsNextBActionPerformed
        next(ConfigType.HANDS);
}//GEN-LAST:event_handsNextBActionPerformed

    private void eyeColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eyeColorBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eyeColorBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeB;
    private javax.swing.JButton eyeColorB;
    private javax.swing.JButton feetNextB;
    private javax.swing.JButton feetPrevB;
    private javax.swing.JButton hairColorB;
    private javax.swing.JButton hairNextB;
    private javax.swing.JButton hairPrevB;
    private javax.swing.JButton handsNextB;
    private javax.swing.JButton handsPrevB;
    private javax.swing.JButton headNextB;
    private javax.swing.JButton headPrevB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jacketNextB;
    private javax.swing.JButton jacketPrevB;
    private javax.swing.JButton legsNextB;
    private javax.swing.JButton legsPrevB;
    private javax.swing.JButton pantsColorB;
    private javax.swing.JButton shirtColorB;
    private javax.swing.JButton shoesColorB;
    private javax.swing.JButton skinColorB;
    private javax.swing.JButton torsoNextB;
    private javax.swing.JButton torsoPrevB;
    // End of variables declaration//GEN-END:variables

}
