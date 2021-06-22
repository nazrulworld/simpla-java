# Disclaimer about License
The purpose of this shared (inside pdf-tables directory) codebase sole for learning/demonstration.
For example to show author's proficiency on specific language and obviously as learning material for others (where applicable).

This is not about to distribute source code as software by any means.

Open Source libraries are used i) itextpdf (under AGPL v3), ii) jackson-databind (under Apache 2.0). If you
want to use in your project, please see their terms and conditions.


Goal
====
Generation of pdf tables from json file.
1. Load json file content from file system and load into Object mapping.
2. Create pdf table and pour with data and exposed into page.
	1. Control the position, size of table.
	2. Some string manipulation, for example ensure font weight bold inside string of <b>tag.
3. Header & Footer
   1. Putting page number.

