This Project is an implementation of Huffman encoding which is a lossless compression algorithm. The program con convert from a .txt into an encoded .hf fileand vice-versa. There are 2 header formats that the algorithm can create or read from: a Standard Count Format which is a table of characters and their frequency, and a Standard Tree Format which contains the information of the Huffman Tree. This project was an assignment in my Data Structures class and parts of the program was provided to me. 

Analysis on performance.
Text-based files seemed to have better compression percentages when using the huffman coding algorithm. The Calgary files 
had a total compression of around 43% for both header formats and the BooksAndHTML files had a total compression of around
40%. On the other hand, the file in the Waterloo directory had around 18% compression. These files were images as opposed to 
texts. Therefore, it can be said that our implementation of Huffman coding is more effective with text files over images.

Compressing already compressed files:
We ran the compression algorith over some compressed files and double compressed files to see what would happen. What we saw
that barely any compression occured and in some cases, the file after compression was larger. This means that we can't simply
run the hufmman coding algorithm on the same file agin and again and get smaller and smaller files. This is likely due to the 
fact that the compressed file can have multiple letters or "bits of information" with in the 8 bits that the preprocessor reads.
Therefore it is less likely to find repeating strings of 8 bits to compress to smaller quantities. 

