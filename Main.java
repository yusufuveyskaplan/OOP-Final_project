import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main extends JFrame {
    private JPanel panel1;
    private JTextField yeniKonumTextField;
    private JTextField tasinacakDizinTextField;
    private JCheckBox gizleCheckBox;
    private JCheckBox ZipleCheckBox;
    private JCheckBox sifreleCheckBox;
    private JRadioButton tumDosyalarRadioButton;
    private JRadioButton sadeceTxtRadioButton;
    private JRadioButton sadecePdfRadioButton;
    private JRadioButton sadeceDocxRadioButton;
    private JButton tasiButton;
    private JCheckBox sifreyiCozCheckBox;
    private JButton gozatButton;
    private JButton gozatButton1;

    public Main() {
        setContentPane(panel1);
        setTitle("Dosya işlemleri");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(tumDosyalarRadioButton);
        bg.add(sadeceTxtRadioButton);
        bg.add(sadecePdfRadioButton);
        bg.add(sadeceDocxRadioButton);

        gozatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File kaynakDizin = fileChooser.getSelectedFile();
                    tasinacakDizinTextField.setText(kaynakDizin.getAbsolutePath());
                }
            }
        });

        gozatButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File hedefDizin = fileChooser.getSelectedFile();
                    yeniKonumTextField.setText(hedefDizin.getAbsolutePath());
                }
            }
        });


        tasiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String kaynakDizinYolu = tasinacakDizinTextField.getText();
                String hedefDizinYolu = yeniKonumTextField.getText();

                File kaynakDizini = new File(kaynakDizinYolu);
                File hedefDizini = new File(hedefDizinYolu);

                if (!kaynakDizini.exists()) {
                    JOptionPane.showMessageDialog(null, "Kaynak dizin bulunamadı.");
                    return;
                }
                if (!hedefDizini.exists()) {
                    JOptionPane.showMessageDialog(null, "Hedef dizin bulunamadı.");
                    return;
                }

                String[] dosyaUzantisi = getSecilmisDosyaUzantisi();
                File[] dosyalar = kaynakDizini.listFiles();

                if (dosyalar != null) {
                    for (File dosya : dosyalar) {
                        if (dosya.isFile() && hasGecerliUzanti(dosya, dosyaUzantisi)) {
                            try {
                                String dosyaIsmi = dosya.getName();
                                File hedefDosya = new File(hedefDizinYolu + File.separator + dosyaIsmi);

                                if (sifreleCheckBox.isSelected()) {
                                    // Dosya içeriğini şifreleme işlemini gerçekleştir
                                    if (sifreleCheckBox.isSelected() && sifreyiCozCheckBox.isSelected()) {
                                        JOptionPane.showMessageDialog(null, "Şifreleme ve çözme işlemi aynı anda yapılamaz.");
                                        break;
                                    } else {
                                        Sifrele sif = new Sifrele();
                                        sif.sifrelenmisDosyaIcerigi(dosya, hedefDosya);
                                    }
                                } else if (sifreyiCozCheckBox.isSelected()) {
                                    // Dosya içeriğini şifre çözme işlemini gerçekleştir
                                    Sifrele coz = new Sifrele();
                                    coz.cozulecekDosyaIcerigi(dosya, hedefDosya);
                                }
                                else {
                                    // Dosya taşıma işlemini gerçekleştir
                                    Files.move(dosya.toPath(), hedefDosya.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                }

                                if (gizleCheckBox.isSelected()) {
                                    // Dosyayı gizleme işlemini gerçekleştir
                                    if (!hedefDosya.isHidden()) {
                                        Files.setAttribute(hedefDosya.toPath(), "dos:hidden", true);
                                        Files.move(dosya.toPath(), hedefDosya.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                }

                                if (ZipleCheckBox.isSelected()) {
                                    // Dosyayı Zipleme işlemini gerçekleştir
                                    String zipDosyaYolu = hedefDizinYolu + File.separator + "sikistirilmis" + File.separator + dosyaIsmi + ".zip";
                                    zipDosyasi(hedefDosya, zipDosyaYolu);
                                }

                            } catch (Exception ex) {

                            }
                        }
                    }
                }
            }
        });

        gizleCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String hedefDosyaYolu = yeniKonumTextField.getText();
                File hedefDosya = new File(hedefDosyaYolu);

                if (!hedefDosya.exists()) {
                    JOptionPane.showMessageDialog(null, "Hedef dizin bulunamadı.");
                    return;
                }

                File[] dosyalar = hedefDosya.listFiles();

                if (dosyalar != null) {
                    for (File dosya : dosyalar) {
                        if (gizleCheckBox.isSelected()) {
                            // Dosyayı gizleme işlemini gerçekleştir
                            if (!dosya.isHidden()) {
                                try {
                                    Files.setAttribute(dosya.toPath(), "dos:hidden", true);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "Dosya gizleme hatası: " + ex.getMessage());
                                }
                            }
                        } else {
                            if (dosya.isHidden()) {
                                try {
                                    Files.setAttribute(dosya.toPath(), "dos:hidden", false);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "Dosya açma hatası: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        });
    }


    private String[] getSecilmisDosyaUzantisi() {
        if (tumDosyalarRadioButton.isSelected()) {
            return new String[]{".txt", ".pdf", ".docx"};
        } else if (sadeceTxtRadioButton.isSelected()) {
            return new String[]{".txt"};
        } else if (sadecePdfRadioButton.isSelected()) {
            return new String[]{".pdf"};
        } else if (sadeceDocxRadioButton.isSelected()) {
            return new String[]{".docx"};
        }
        return new String[]{};
    }

    private boolean hasGecerliUzanti(File dosya, String[] uzanti) {
        String dosyaAdi = dosya.getName();
        for (String extension : uzanti) {
            if (dosyaAdi.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    private void zipDosyasi(File kaynakDosya, String zipDosyaYolu) throws IOException {
        File zipDosyasi = new File(zipDosyaYolu);
        File parentDir = zipDosyasi.getParentFile();

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(zipDosyasi);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry zipEntry = new ZipEntry(kaynakDosya.getName());
            zos.putNextEntry(zipEntry);

            // Dosyanın içeriğini zip dosyasına yazma işlemi
            try (FileInputStream fis = new FileInputStream(kaynakDosya)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }
            kaynakDosya.delete();
            zos.closeEntry();
        }
    }



    class Sifrele extends Main {
        private void sifrelenmisDosyaIcerigi(File kaynakDosya, File hedefDosya) throws IOException {
            try (BufferedReader okuyucu = new BufferedReader(new FileReader(kaynakDosya));
                 BufferedWriter yazici = new BufferedWriter(new FileWriter(hedefDosya))) {
                String satir;
                while ((satir = okuyucu.readLine()) != null) {
                    // Satırı şifrele
                    String sifrelenecekSatir = sifreleString(satir);
                    // Şifrelenmiş satırı hedef dosyaya yaz
                    yazici.write(sifrelenecekSatir);
                    yazici.newLine();
                }
            }
            kaynakDosya.delete();
        }


        private String sifreleString(String satir) {
            int otele = Integer.parseInt(JOptionPane.showInputDialog("Metin Kaç Karakter Ötelensin: (Girdiğiniz Sayıyı Unutmayın)"));
            StringBuilder sifrelenmisMetin = new StringBuilder();
            for (int i = 0; i < satir.length(); i++) {
                char c = satir.charAt(i);
                // Sadece harfleri ötele, diğer karakterler aynı kalsın
                if (Character.isLetter(c)) {
                    // Büyük harfse A-Z aralığında ötele
                    if (Character.isUpperCase(c)) {
                        c = (char) ((c - 'A' + otele) % 26 + 'A');
                    }
                    // Küçük harfse a-z aralığında ötele
                    else {
                        c = (char) ((c - 'a' + otele) % 26 + 'a');
                    }
                }
                sifrelenmisMetin.append(c);
            }
            return sifrelenmisMetin.toString();
        }

        private void cozulecekDosyaIcerigi(File kaynakDosya, File hedefDosya) throws IOException {
            try (BufferedReader okuyucu = new BufferedReader(new FileReader(kaynakDosya));
                 BufferedWriter yazici = new BufferedWriter(new FileWriter(hedefDosya))) {
                String satir;
                while ((satir = okuyucu.readLine()) != null) {
                    // Satırı çöz
                    String cozulmusSatir = cozString(satir);
                    // Çözülmüş satırı hedef dosyaya yaz
                    yazici.write(cozulmusSatir);
                    yazici.newLine();
                }
            }
            kaynakDosya.delete();
        }

        private String cozString(String satir) {
            // Şifre çözme algoritmasını uygula
            int otele =Integer.parseInt(JOptionPane.showInputDialog("Metni Kaç Karakter Ötelediniz:"));
            StringBuilder cozulmusMetin = new StringBuilder();
            for (int i = 0; i < satir.length(); i++) {
                char c = satir.charAt(i);
                // Sadece harfleri ötele, diğer karakterler aynı kalsın
                if (Character.isLetter(c)) {
                    // Büyük harfse Z-A aralığında ötele
                    if (Character.isUpperCase(c)) {
                        c = (char) ((c - 'Z' - otele) % 26 + 'Z');
                    }
                    // Küçük harfse z-a aralığında ötele
                    else {
                        c = (char) ((c - 'z' - otele) % 26 + 'z');
                    }
                }
                cozulmusMetin.append(c);
            }
            return cozulmusMetin.toString();
        }

    }
    public static void main(String[] args) {
        new Main();
    }
}