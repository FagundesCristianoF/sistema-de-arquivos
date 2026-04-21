package operatingSystem.fileSystem

import infra.ConsoleLogger
import infra.Logger
import operatingSystem.Kernel
import java.awt.event.KeyEvent

class FileSystemSimulator(
    private var myKernel: Kernel?,
    private val logger: Logger,
) : javax.swing.JFrame() {
    private var lastCommand = ""
    private var lastResult = ""
    private var base = ""
    private var history: MutableList<String> = mutableListOf()
    private var position = 0

    /**
     * Creates new form SSH
     */
    init {
        initComponents()
        if (myKernel == null) {
            logger.error("Invalid kernel.")
            System.exit(1)
        } else {
            this.base = "Cristiano@cristiano:"
            writePrompt()
            history.add("")
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private fun initComponents() {
        txtCommand = javax.swing.JTextField()
        jScrollPane3 = javax.swing.JScrollPane()
        jScrollPane1 = javax.swing.JScrollPane()
        textArea = javax.swing.JTextArea()
        defaultCloseOperation = javax.swing.WindowConstants.EXIT_ON_CLOSE
        title = "File System Simulator"
        background = java.awt.Color(1, 1, 1)
        txtCommand.background = java.awt.Color(0, 0, 0)
        txtCommand.font = java.awt.Font("Courier New", 0, 14)
        txtCommand.foreground = java.awt.Color(255, 255, 255)
        txtCommand.text = "student@localhost:~$"
        txtCommand.caretColor = java.awt.Color(255, 255, 255)
        txtCommand.disabledTextColor = java.awt.Color(0, 0, 0)
        txtCommand.addActionListener { evt -> txtCommandActionPerformed(evt) }
        txtCommand.addKeyListener(
            object : java.awt.event.KeyAdapter() {
                override fun keyPressed(evt: java.awt.event.KeyEvent) {
                    txtCommandKeyPressed(evt)
                }

                override fun keyReleased(evt: java.awt.event.KeyEvent) {
                    txtCommandKeyReleased(evt)
                }
            },
        )
        textArea.background = java.awt.Color(0, 0, 0)
        textArea.columns = 20
        textArea.isEditable = false
        textArea.font = java.awt.Font("Courier New", 0, 14)
        textArea.foreground = java.awt.Color(255, 255, 255)
        textArea.rows = 5
        jScrollPane1.setViewportView(textArea)
        jScrollPane3.setViewportView(jScrollPane1)
        val layout = javax.swing.GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
            layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(
                    txtCommand,
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    502,
                    Int.MAX_VALUE,
                ).addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Int.MAX_VALUE),
        )
        layout.setVerticalGroup(
            layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    layout
                        .createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Int.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE),
                ),
        )
        pack()
        setLocationRelativeTo(null)
    } // </editor-fold>//GEN-END:initComponents

    private fun txtCommandKeyPressed(evt: java.awt.event.KeyEvent) { // GEN-FIRST:event_txtCommandKeyPressed
        if (evt.keyCode == KeyEvent.VK_ENTER) {
            var command = txtCommand.text
            appendToTextArea(txtCommand.text)
            command = command.substring((base.length + FileSystemSimulator.currentDir.length + 2), command.length)
            dispatchCommand(command)
            history.add(command)
            writeTextArea()
            writePrompt()
        }
        if (evt.keyCode == KeyEvent.VK_UP) {
            if (position == 0) {
                position = history.size - 1
                txtCommand.text = base + FileSystemSimulator.currentDir + "$ " + history[position]
            } else {
                position--
                txtCommand.text = base + FileSystemSimulator.currentDir + "$ " + history[position]
            }
        }
        if (evt.keyCode == KeyEvent.VK_DOWN) {
            if (position + 1 == history.size) {
                position = 0
                txtCommand.text = base + FileSystemSimulator.currentDir + "$ " + history[position]
            } else {
                position++
                txtCommand.text = base + FileSystemSimulator.currentDir + "$ " + history[position]
            }
        }
        if (evt.keyCode == KeyEvent.VK_DELETE) {
            txtCommand.text = base + FileSystemSimulator.currentDir + "$ "
        }
    } // GEN-LAST:event_txtCommandKeyPressed

    @Suppress("UNUSED_PARAMETER")
    private fun txtCommandKeyReleased(evt: java.awt.event.KeyEvent) { // GEN-FIRST:event_txtCommandKeyReleased
        if (txtCommand.text.length <= (base.length + FileSystemSimulator.currentDir.length + 2) || txtCommand.text.isEmpty()) {
            writePrompt()
        }
    } // GEN-LAST:event_txtCommandKeyReleased

    @Suppress("UNUSED_PARAMETER")
    private fun txtCommandActionPerformed(evt: java.awt.event.ActionEvent) { // GEN-FIRST:event_txtCommandActionPerformed
        // No-op
    } // GEN-LAST:event_txtCommandActionPerformed

    /**
     * @param args the command line arguments
     */
    companion object {
        @JvmField
        var currentDir: String = "/"

        @JvmStatic
        fun main(args: Array<String>) {
            java.awt.EventQueue.invokeLater {
                FileSystemSimulator(null, ConsoleLogger()).isVisible = true
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private lateinit var jScrollPane1: javax.swing.JScrollPane
    private lateinit var jScrollPane3: javax.swing.JScrollPane
    private lateinit var textArea: javax.swing.JTextArea
    private lateinit var txtCommand: javax.swing.JTextField
    // End of variables declaration//GEN-END:variables

    private fun dispatchCommand(command: String) {
        if (command.trim().isEmpty()) return
        val args = command.trim().split(" ")
        val arg = if (args.size > 1) command.trim().substringAfter("${args[0]} ") else ""
        when (args[0]) {
            "cd" -> lastResult = myKernel!!.cd(arg)
            "ls" -> lastResult = myKernel!!.ls(arg)
            "mkdir" -> lastResult = myKernel!!.mkdir(arg)
            "rmdir" -> lastResult = myKernel!!.rmdir(arg)
            "cp" -> lastResult = myKernel!!.cp(arg)
            "mv" -> lastResult = myKernel!!.mv(arg)
            "rm" -> lastResult = myKernel!!.rm(arg)
            "chmod" -> lastResult = myKernel!!.chmod(arg)
            "createfile" -> lastResult = myKernel!!.createfile(arg)
            "clear" -> textArea.text = ""
            "cat" -> lastResult = myKernel!!.cat(arg)
            "batch" -> lastResult = myKernel!!.batch(arg)
            "dump" -> lastResult = if (args.size > 1) myKernel!!.dump(arg) else myKernel!!.batch("")
            "info" -> lastResult = myKernel!!.info()
            "exit" -> System.exit(0)
            else -> lastResult = "$command: Invalid command."
        }
    }

    private fun writePrompt() {
        txtCommand.text = base + FileSystemSimulator.currentDir + "$ "
    }

    private fun writeTextArea() {
        if (lastResult == "") {
            textArea.append(lastCommand)
        } else {
            textArea.append(lastCommand + "\n" + lastResult)
        }
        lastCommand = ""
        lastResult = ""
    }

    private fun appendToTextArea(str: String) {
        lastCommand += "\n$str"
    }
}
