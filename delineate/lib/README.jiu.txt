JIU - Java Imaging Utilities - README file

JIU is a Java library to load, process, analyze and save pixel images.

The project's homepage is at <http://jiu.sourceforge.net>.

If you want to give feedback (questions, suggestions, bug reports), 
please write a message on the Open Discussion Forum at 
<http://sourceforge.net/forum/forum.php?forum_id=51534>. Please
check the subjects, the question you want to ask may already have been
answered.

You can be notified about new releases by subscribing to JIU on
its Freshmeat page <http://freshmeat.net/projects/jiu/> (under
"Subscribe to new releases"). If you don't have a Freshmeat login
yet you can get one for free.

The library is distributed under the GNU General Public License version 
2. See the LICENSE file that is part of the distribution or visit 
<http://www.gnu.org/copyleft/gpl.html>.

Documentation right now consists of the following items:
* ChangeLog - changes in reverse chronological order
* TODO - what's to be changed / added / removed next
* API docs - the classes, their methods and fields, as HTML / PDF / PS / DVI
* Manual - a general introduction (only in an early stage)

In order to get an impression of what JIU can do, start the jiuawt demo
program that is part of the library. You must have a Java Runtime Environment
version 1.1 or higher installed (typing "java -version" on the command 
line should tell you whether you have one and which version it is). Once 
you have downloaded and decompressed the non-core version archive of JIU, 
go to the directory where you have put jiu.jar (it is in the root directory
of the archive) and start it

* by typing "java -jar jiu.jar" or
* by double-clicking on jiu.jar in a file manager.
* by typing "java -cp jiu.jar net.sourceforge.jiu.apps.jiuawt" or

jiuawt requires Java 1.1+, so it should run with most virtual machines
in use. Note that by default Java VMs only get to use a certain amount
of memory. With images one easily reaches that limit. Provide the VM
with more memory by starting it with -mx<MB>m as parameter between java and
-cp (or -jar). So java -mx256m -jar jiu.jar would start the program and
give 256 MB to it. You may want to adjust that value according to your
needs.