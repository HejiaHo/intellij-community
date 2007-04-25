package com.intellij.localvcsintegr;


import com.intellij.localvcs.core.Clock;
import com.intellij.localvcs.core.LocalVcs;
import com.intellij.localvcs.core.revisions.Revision;
import com.intellij.localvcs.core.storage.Storage;
import com.intellij.localvcs.integration.LocalHistory;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.List;

public class BasicsTest extends IntegrationTestCase {
  public void testComponentInitialization() {
    assertNotNull(getVcsComponent());
  }

  public void testSaving() throws Exception {
    VirtualFile f = root.createChildData(null, "file.java");
    myProject.save();

    Storage s = new Storage(getVcsComponent().getStorageDir());
    LocalVcs vcs = new LocalVcs(s);
    s.close();
    assertTrue(vcs.hasEntry(f.getPath()));
  }

  public void testProcessingCommands() throws Exception {
    final VirtualFile[] f = new VirtualFile[1];

    CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
      public void run() {
        try {
          f[0] = root.createChildData(null, "f1.java");
          f[0].setBinaryContent(new byte[]{1});
          f[0].setBinaryContent(new byte[]{2});
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }, "name", null);

    assertEquals(1, getVcsRevisionsFor(f[0]).size());
  }

  public void testUpdatingOnFileTypesChange() throws Exception {
    VirtualFile f = root.createChildData(null, "file.xxx");

    assertFalse(hasVcsEntry(f));

    FileTypeManager tm = FileTypeManager.getInstance();
    tm.registerFileType(StdFileTypes.PLAIN_TEXT, "xxx");

    assertTrue(hasVcsEntry(f));

    tm.removeAssociatedExtension(StdFileTypes.PLAIN_TEXT, "xxx");

    assertFalse(hasVcsEntry(f));
  }

  public void testPuttingLabel() throws IOException {
    VirtualFile f = root.createChildData(null, "file.java");

    assertEquals(1, getVcsRevisionsFor(f).size());
    assertEquals(2, getVcsRevisionsFor(root).size());

    LocalHistory.putLabel(myProject, f.getPath(), "file");
    LocalHistory.putLabel(myProject, "global");

    List<Revision> rr = getVcsRevisionsFor(f);
    assertEquals(3, rr.size());
    assertEquals("global", rr.get(0).getName());
    assertEquals("file", rr.get(1).getName());

    rr = getVcsRevisionsFor(root);
    assertEquals(3, rr.size());
    assertEquals("global", rr.get(0).getName());
  }

  public void testIsUnderControl() throws Exception {
    VirtualFile f1 = root.createChildData(null, "file.java");
    VirtualFile f2 = root.createChildData(null, "file.xxx");

    assertTrue(LocalHistory.isUnderControl(myProject, f1));
    assertFalse(LocalHistory.isUnderControl(myProject, f2));
  }

  public void testContentAtDate() throws Exception {
    VirtualFile f = root.createChildData(null, "f.java");
    Clock.setCurrentTimestamp(10);
    f.setBinaryContent(new byte[]{1});
    Clock.setCurrentTimestamp(20);
    f.setBinaryContent(new byte[]{2});

    assertEquals(1, LocalHistory.getByteContentAt(myProject, f, 10)[0]);
    assertEquals(1, LocalHistory.getByteContentAt(myProject, f, 15)[0]);
    assertEquals(2, LocalHistory.getByteContentAt(myProject, f, 20)[0]);
    assertEquals(2, LocalHistory.getByteContentAt(myProject, f, 30)[0]);
  }

  public void testContentAtDateForFilteredFilesIsNull() throws Exception {
    VirtualFile f = root.createChildData(null, "f.xxx");
    Clock.setCurrentTimestamp(10);
    f.setBinaryContent(new byte[]{1});

    assertNull(LocalHistory.getByteContentAt(myProject, f, 20));
  }

  public void testMarkedContent() throws Exception {
    VirtualFile f = root.createChildData(null, "f.java");
    f.setBinaryContent(new byte[]{1});
    LocalHistory.mark(myProject, f);
    f.setBinaryContent(new byte[]{2});

    assertEquals(1, LocalHistory.getLastMarkedByteContent(myProject, f)[0]);
  }

  public void testMarkedContentForFilteredFilesIsNull() throws Exception {
    VirtualFile f = root.createChildData(null, "f.xxx");
    f.setBinaryContent(new byte[]{1});
    LocalHistory.mark(myProject, f);
    f.setBinaryContent(new byte[]{2});

    assertNull(LocalHistory.getLastMarkedByteContent(myProject, f));
  }
}
